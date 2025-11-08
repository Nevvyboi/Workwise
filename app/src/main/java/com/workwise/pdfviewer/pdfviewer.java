package com.workwise.pdfviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.workwise.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class pdfviewer extends AppCompatActivity {

    public static final String EXTRA_PDF_URL = "pdf_url";
    public static final String EXTRA_FILE_NAME = "file_name";
    public static final String EXTRA_CV_URI = "cv_uri";

    private PDFView pdfView;
    private ProgressBar progressBar;
    private TextView pageInfo;
    private TextView titleText;
    private ImageButton nextPageButton;
    private ImageButton previousPageButton;
    private ImageButton closeButton;

    private int currentPage = 0;
    private int totalPages = 0;
    private File downloadedFile = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdfviewer);

        initializeViews();
        setupClickListeners();
        handleIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up downloaded file
        if (downloadedFile != null && downloadedFile.exists()) {
            downloadedFile.delete();
        }
    }

    private void initializeViews() {
        pdfView = findViewById(R.id.pdfView);
        progressBar = findViewById(R.id.progressBar);
        pageInfo = findViewById(R.id.pageInfo);
        titleText = findViewById(R.id.titleText);
        nextPageButton = findViewById(R.id.nextPageButton);
        previousPageButton = findViewById(R.id.previousPageButton);
        closeButton = findViewById(R.id.closeButton);
    }

    private void setupClickListeners() {
        closeButton.setOnClickListener(v -> finish());
        nextPageButton.setOnClickListener(v -> nextPage());
        previousPageButton.setOnClickListener(v -> previousPage());
    }

    private void handleIntent() {
        Intent intent = getIntent();

        // Set title if provided
        String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
        if (fileName != null && titleText != null) {
            titleText.setText(fileName);
        }

        // Check for URL (from API)
        String pdfUrl = intent.getStringExtra(EXTRA_PDF_URL);
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            if (pdfUrl.startsWith("http://") || pdfUrl.startsWith("https://")) {
                // Download from URL
                new DownloadPdfTask().execute(pdfUrl);
            } else if (pdfUrl.startsWith("file://")) {
                // Local file
                Uri fileUri = Uri.parse(pdfUrl);
                loadPdfFromUri(fileUri);
            } else {
                Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }

        // Check for URI (from file picker)
        Uri pdfUri = intent.getParcelableExtra(EXTRA_CV_URI);
        if (pdfUri != null) {
            loadPdfFromUri(pdfUri);
            return;
        }

        // No valid PDF source
        Toast.makeText(this, "No PDF to display", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadPdfFromUri(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);

        pdfView.fromUri(uri)
                .defaultPage(0)
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(false)
                .spacing(10)
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        currentPage = page;
                        totalPages = pageCount;
                        updatePageInfo();
                    }
                })
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        totalPages = nbPages;
                        progressBar.setVisibility(View.GONE);
                        updatePageInfo();
                    }
                })
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(pdfviewer.this, "Error loading PDF: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .load();
    }

    private void loadPdfFromFile(File file) {
        progressBar.setVisibility(View.VISIBLE);

        pdfView.fromFile(file)
                .defaultPage(0)
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(false)
                .spacing(10)
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        currentPage = page;
                        totalPages = pageCount;
                        updatePageInfo();
                    }
                })
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        totalPages = nbPages;
                        progressBar.setVisibility(View.GONE);
                        updatePageInfo();
                    }
                })
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(pdfviewer.this, "Error loading PDF: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .load();
    }

    private void updatePageInfo() {
        runOnUiThread(() -> {
            if (pageInfo != null && totalPages > 0) {
                pageInfo.setText(String.format("Page %d of %d", currentPage + 1, totalPages));
            }
        });
    }

    private void nextPage() {
        if (pdfView != null && currentPage < totalPages - 1) {
            currentPage++;
            pdfView.jumpTo(currentPage, true);
            updatePageInfo();
        }
    }

    private void previousPage() {
        if (pdfView != null && currentPage > 0) {
            currentPage--;
            pdfView.jumpTo(currentPage, true);
            updatePageInfo();
        }
    }

    private class DownloadPdfTask extends AsyncTask<String, Void, File> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected File doInBackground(String... urls) {
            String urlString = urls[0];

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // Create temp file
                File tempFile = new File(getCacheDir(), "temp_cv_" + System.currentTimeMillis() + ".pdf");

                InputStream input = new BufferedInputStream(connection.getInputStream());
                FileOutputStream output = new FileOutputStream(tempFile);

                byte[] data = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        output.close();
                        return null;
                    }
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                connection.disconnect();

                return tempFile;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);

            if (file != null && file.exists()) {
                downloadedFile = file;
                loadPdfFromFile(file);
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(pdfviewer.this, "Failed to download PDF", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}