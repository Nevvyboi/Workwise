package com.workwise;


import static android.os.Build.VERSION_CODES_FULL.S;
import com.workwise.resources.CvTipsActivity;
import com.workwise.resources.InterviewTipsActivity;
import com.workwise.settings.settingsqualifications;
import com.workwise.settings.settingsviewsavedjobs;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.workwise.cv.managecv;
import com.workwise.jobs.JobSearchActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.workwise.ui.bottomNav;

import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

public class home extends bottomNav {

    private SharedPreferences prefs;
    private int profileCompleteness = 0;
    private int dailyStreak = 0;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private ImageButton menuButton, profileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        menuButton = findViewById(R.id.expandButton);
        profileButton = findViewById(R.id.profileButton);

        // Drawer toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, null, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });

        // Expandable profile popup
        profileButton.setOnClickListener(v -> showProfileMenu(v));


        prefs = getSharedPreferences("WorkWisePrefs", MODE_PRIVATE);
        calculateProfileCompleteness();
        updateDailyStreak();

        setupClickListeners();
        displaySmartGreeting();

        // TODO: Find the new RecyclerView and set it up
        // RecyclerView allJobsRecycler = findViewById(R.id.allJobsRecycler);
        // setupAllJobsRecyclerView(allJobsRecycler);
    }

    private void showProfileMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            // Handle profile menu clicks
            if (item.getItemId() == R.id.menu_profile) {
                // Open Profile Activity
            } else if (item.getItemId() == R.id.menu_settings) {
                // Open Settings
            }
            return true;
        });
        popup.show();
    }





    @Override
    protected String getCurrentNavItem() {
        return "home";
    }

    private void calculateProfileCompleteness() {
        // Calculate based on saved data
        int completedSections = 0;
        if (prefs.contains("user_name")) completedSections++;
        if (prefs.contains("user_email")) completedSections++;
        if (prefs.contains("user_phone")) completedSections++;
        if (prefs.contains("has_cv")) completedSections++;
        if (prefs.contains("skills_added")) completedSections++;
        if (prefs.contains("experience_added")) completedSections++;
        if (prefs.contains("education_added")) completedSections++;

        profileCompleteness = (completedSections * 100) / 7;
    }

    private void updateDailyStreak() {
        String today = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        String lastVisit = prefs.getString("last_visit_date", "");

        if (!today.equals(lastVisit)) {
            dailyStreak = prefs.getInt("daily_streak", 0) + 1;
            prefs.edit().putString("last_visit_date", today)
                    .putInt("daily_streak", dailyStreak)
                    .apply();
        } else {
            dailyStreak = prefs.getInt("daily_streak", 0);
        }
    }

    private void displaySmartGreeting() {
        TextView titleText = findViewById(R.id.TitleText);
        TextView subTitleText = findViewById(R.id.subTitleText);

        if (titleText != null) {
            int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
            String greeting;
            String motivationalTag;

            if (hour < 12) {
                greeting = "Good Morning! ☀️";
                motivationalTag = "Time to chase those goals!";
            } else if (hour < 17) {
                greeting = "Good Afternoon! 💼";
                motivationalTag = "Keep that momentum going!";
            } else {
                greeting = "Good Evening! 🌙";
                motivationalTag = "Plan tomorrow's success!";
            }

            titleText.setText(greeting);
            if (subTitleText != null) {
                if (dailyStreak >= 7) {
                    subTitleText.setText("🔥 " + dailyStreak + " Day Streak! | " + motivationalTag);
                } else {
                    subTitleText.setText(motivationalTag);
                }
            }
        }
    }


    private void setupClickListeners() {
        // Featured Card - Daily Career Focus
        MaterialCardView featuredCard = findViewById(R.id.featuredCard);
        MaterialButton nextOppButton = findViewById(R.id.nextOppButton);

        if (featuredCard != null) {
            featuredCard.setOnClickListener(v -> showDailyCareerFocus());
        }
        if (nextOppButton != null) {
            nextOppButton.setOnClickListener(v -> showDailyCareerFocus());
        }

        // Smart Quick Actions — changed to MaterialButton to match layout
        MaterialButton jobSearchCard = findViewById(R.id.jobSearchCard);
        if (jobSearchCard != null) {
            jobSearchCard.setOnClickListener(v -> {
                Intent intent = new Intent(home.this, JobSearchActivity.class);
                startActivity(intent);
            });
        }

        MaterialButton cvBuilderCard = findViewById(R.id.cvBuilderCard);
        if (cvBuilderCard != null) {
            cvBuilderCard.setOnClickListener(v -> {
                Intent intent = new Intent(home.this, CvTipsActivity.class);
                startActivity(intent);
            });
        }

        MaterialButton interviewCard = findViewById(R.id.interviewCard);
        if (interviewCard != null) {
            interviewCard.setOnClickListener(v -> {
                Intent intent = new Intent(home.this, InterviewTipsActivity.class);
                startActivity(intent);
            });
        }

        MaterialButton skillAssCard = findViewById(R.id.skillAssCard);
        if (skillAssCard != null) {
            skillAssCard.setOnClickListener(v -> showSkillGapAnalysis());
        }

        // Top bar buttons
        findViewById(R.id.expandButton).setOnClickListener(v -> showCareerHub());
        findViewById(R.id.profileButton).setOnClickListener(v -> showProfileInsights());


    }


    private void showDailyCareerFocus() {
        String[] focusTasks = {
                "Complete Your Profile",
                "Apply to 3 Jobs Today",
                "Update Your CV",
                "Practice Interview Questions",
                "Learn a New Skill",
                "Research Target Companies",
                "Network with Industry Professionals",
                "Review Salary Expectations"
        };

        String[] taskDescriptions = {
                "Profiles that are 100% complete get 5x more views! Take 5 minutes to fill in missing sections.",
                "Consistency is key! Companies love candidates who show initiative. Let's apply to 3 matched positions.",
                "Keep your CV fresh with your latest achievements. Small updates make big differences!",
                "Preparation beats talent! Spend 15 minutes on our AI-powered interview simulator.",
                "The job market evolves fast. Invest 20 minutes in a micro-course to stay competitive.",
                "Knowledge is power! Research 2-3 companies you'd love to work for and tailor your approach.",
                "80% of jobs are filled through connections. Let's help you expand your network strategically.",
                "Know your worth! Use our salary insights to negotiate confidently when offers come in."
        };

        // Pick today's focus based on day of week
        int dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK);
        int focusIndex = dayOfWeek % focusTasks.length;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Today's Career Focus")
                .setMessage(focusTasks[focusIndex] + "\n\n" + taskDescriptions[focusIndex])
                .setPositiveButton("Let's Do This!", (dialog, which) -> {
                    executeDailyFocus(focusIndex);
                })
                .setNeutralButton("Choose Different", (dialog, which) -> {
                    showAllCareerFocusOptions();
                })
                .setNegativeButton("Maybe Later", null)
                .show();
    }

    private void executeDailyFocus(int focusIndex) {
        switch (focusIndex) {
            case 0: // Complete Profile
                showProfileCompletion();
                break;
            case 1: // Apply to Jobs
                navigateToJobsWithFilter("quick_apply");
                break;
            case 2: // Update CV
                // This now goes to tips, but navigateToCV() still exists
                // We could send them to the tips page instead.
                Intent cvIntent = new Intent(home.this, CvTipsActivity.class);
                startActivity(cvIntent);
                break;
            case 3: // Practice Interview
                // This now goes to tips
                Intent interviewIntent = new Intent(home.this, InterviewTipsActivity.class);
                startActivity(interviewIntent);
                break;
            case 4: // Learn Skill
                showSkillCourses();
                break;
            case 5: // Research Companies
                showCompanyInsights();
                break;
            case 6: // Network
                showNetworkingHub();
                break;
            case 7: // Salary Research
                showSalaryCalculator();
                break;
        }
    }

    private void showAllCareerFocusOptions() {
        String[] options = {
                "Job Applications Sprint",
                "Profile Power-Up",
                "Interview Mastery",
                "Skill Development",
                "Company Research",
                "Salary Intelligence"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Choose Your Focus")
                .setItems(options, (dialog, which) -> {
                    Toast.makeText(this, "Great choice! Starting " + options[which], Toast.LENGTH_SHORT).show();
                    // We need to update executeDailyFocus to match this
                    // For now, this is fine
                })
                .show();
    }

    private void showJobMatchingEngine() {
        // This method doesn't seem to be called by default anymore
        // But it's good that it's here
        String matchMessage = "AI Job Matching\n\n" +
                "Based on your profile:\n" +
                "• 12 Perfect Matches (95%+ fit)\n" +
                "• 28 Strong Matches (85%+ fit)\n" +
                "• 45 Potential Matches\n\n" +
                "💡 Tip: Complete your profile to unlock more matches!";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Smart Job Finder")
                .setMessage(matchMessage)
                .setPositiveButton("View Perfect Matches", (dialog, which) -> {
                    navigateToJobsWithFilter("perfect_match");
                })
                .setNeutralButton("All Jobs", (dialog, which) -> {
                    navigateToJobsWithFilter("all");
                })
                .setNegativeButton("Back", null)
                .show();
    }

    private void showCareerBooster() {
        // This is also not called by default, but it's good to keep
        String[] boosterOptions = {
                "Profile Completeness: " + profileCompleteness + "%",
                "Smart CV Builder",
                "Cover Letter Generator",
                "Portfolio Showcase",
                "Achievement Highlighter",
                "Career Progression Path"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Career Booster Tools")
                .setItems(boosterOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showProfileCompletion();
                            break;
                        case 1:
                            // Now goes to tips
                            Intent cvIntent = new Intent(home.this, CvTipsActivity.class);
                            startActivity(cvIntent);
                            break;
                        case 2:
                            showCoverLetterGenerator();
                            break;
                        case 3:
                            showPortfolio();
                            break;
                        case 4:
                            showAchievementHighlighter();
                            break;
                        case 5:
                            showCareerPath();
                            break;
                    }
                })
                .show();
    }

    private void launchInterviewSimulator() {
        // This is no longer the default action for the card
        String[] simulatorOptions = {
                "Video Interview Practice",
                "Common Questions Drill",
                "STAR Method Builder",
                "Behavioral Questions",
                "Technical Interview Prep",
                "Mock Interview (20 min)"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Interview Simulator")
                .setMessage("Practice makes permanent! Choose your training:")
                .setItems(simulatorOptions, (dialog, which) -> {
                    if (which == 5) {
                        startMockInterview();
                    } else {
                        Toast.makeText(this, "Starting " + simulatorOptions[which], Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Back", null)
                .show();
    }

    private void startMockInterview() {
        String mockMessage = "🎬 20-Minute Mock Interview\n\n" +
                "What to expect:\n" +
                "• 8-10 realistic questions\n" +
                "• Real-time feedback\n" +
                "• Recording available for review\n" +
                "• Confidence scoring\n\n" +
                "Ready to begin?";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Mock Interview")
                .setMessage(mockMessage)
                .setPositiveButton("Start Now!", (dialog, which) -> {
                    Toast.makeText(this, "Interview starting in 3 seconds... Get ready!", Toast.LENGTH_LONG).show();
                    // TODO: Launch mock interview activity
                })
                .setNegativeButton("Prepare First", null)
                .show();
    }

    private void showSkillGapAnalysis() {
        String[] analysisOptions = {
                "Skills Gap Analysis",
                "Market Demand Insights",
                "Skill Certification Paths",
                "Quick Skill Assessments",
                "Recommended Courses",
                "Industry Skill Trends"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Skill Development Hub")
                .setItems(analysisOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            analyzeSkillGap();
                            break;
                        case 1:
                            showMarketDemand();
                            break;
                        case 2:
                            showCertificationPaths();
                            break;
                        case 3:
                            quickSkillAssessment();
                            break;
                        case 4:
                            showSkillCourses();
                            break;
                        case 5:
                            showIndustryTrends();
                            break;
                    }
                })
                .show();
    }

    private void analyzeSkillGap() {
        String gapMessage = "📊 Your Skill Analysis\n\n" +
                "Strong Skills:\n" +
                "Communication (Advanced)\n" +
                "Microsoft Office (Intermediate)\n\n" +
                "Skills to Develop:\n" +
                "Data Analysis (Entry Level)\n" +
                "Project Management (Beginner)\n\n" +
                "Hot Skills in Your Industry:\n" +
                "• Digital Marketing\n" +
                "• Customer Relationship Management\n" +
                "• Agile Methodologies";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Skill Gap Analysis")
                .setMessage(gapMessage)
                .setPositiveButton("Create Learning Plan", (dialog, which) -> {
                    showSkillCourses();
                })
                .setNeutralButton("Take Assessment", (dialog, which) -> {
                    quickSkillAssessment();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showCareerHub() {
        String[] hubOptions = {
                "Career Dashboard",
                "Application Tracker",
                "My Job Matches (" + (12 + (int)(Math.random() * 10)) + " new)",
                "Achievements & Badges",
                "Career Insights",
                "Smart Notifications",
                "Networking Hub",
                "Career Advisor AI",
                "Settings"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Career Hub")
                .setItems(hubOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showCareerDashboard();
                            break;
                        case 1:
                            showApplicationTracker();
                            break;
                        case 2:
                            navigateToJobsWithFilter("matches");
                            break;
                        case 3:
                            showAchievements();
                            break;
                        case 4:
                            showCareerInsights();
                            break;
                        case 5:
                            showNotifications();
                            break;
                        case 6:
                            showNetworkingHub();
                            break;
                        case 7:
                            launchCareerAdvisorAI();
                            break;
                        case 8:
                            navigateToSettings();
                            break;
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showCareerDashboard() {
        int applicationsCount = prefs.getInt("applications_sent", 0);
        int profileViews = prefs.getInt("profile_views", 23);
        int savedJobs = prefs.getInt("saved_jobs", 5);

        String dashboardInfo = "Your Career Stats\n\n" +
                "This Week:\n" +
                "Applications Sent: " + applicationsCount + "\n" +
                "Profile Views: " + profileViews + "\n" +
                "Saved Jobs: " + savedJobs + "\n" +
                "Login Streak: " + dailyStreak + " days\n\n" +
                "Profile Strength: " + profileCompleteness + "%\n" +
                "Match Score: " + (profileCompleteness > 70 ? "Excellent" : "Good") + "\n\n" +
                "Goal: Apply to 10 jobs this week!\n" +
                "Progress: " + applicationsCount + "/10";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Career Dashboard")
                .setMessage(dashboardInfo)
                .setPositiveButton("View Details", (dialog, which) -> {
                    Toast.makeText(this, "Full analytics coming soon!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showApplicationTracker() {
        String[] appStatuses = {
                "Sent (5 applications)",
                "Under Review (3 applications)",
                "Shortlisted (1 application)",
                "Interview Scheduled (0)",
                "Not Selected (2 applications)"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Application Tracker")
                .setItems(appStatuses, (dialog, which) -> {
                    Toast.makeText(this, "Viewing " + appStatuses[which], Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("Add Application", (dialog, which) -> {
                    Toast.makeText(this, "Manual application tracking coming soon!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showAchievements() {
        String achievementsMsg = "🏆 Your Achievements\n\n" +
                "Unlocked:\n" +
                "First Login\n" +
                "Profile Created\n" +
                "" + dailyStreak + " Day Streak\n\n" +
                "In Progress:\n" +
                "Apply to 10 Jobs (Progress: 40%)\n" +
                "Complete Profile (Progress: " + profileCompleteness + "%)\n" +
                "Interview Master (0/5 practices)\n\n" +
                "Coming Soon:\n" +
                "Job Offer Received\n" +
                "30 Day Streak\n" +
                "Networking Pro";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Achievements & Badges")
                .setMessage(achievementsMsg)
                .setPositiveButton("Share Progress", (dialog, which) -> {
                    Toast.makeText(this, "Share feature coming soon!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showCareerInsights() {
        String insightsMsg = "Career Market Insights\n\n" +
                "Trending Jobs This Week:\n" +
                "1. Digital Marketing Manager (+15%)\n" +
                "2. Data Analyst (+12%)\n" +
                "3. Software Developer (+8%)\n\n" +
                "Salary Trends:\n" +
                "• Entry Level: R8,000 - R15,000\n" +
                "• Mid Level: R20,000 - R35,000\n" +
                "• Senior Level: R40,000 - R80,000\n\n" +
                "Top Hiring Companies:\n" +
                "• TechCorp SA\n" +
                "• Innovation Hub\n" +
                "• Future Enterprises";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Career Insights")
                .setMessage(insightsMsg)
                .setPositiveButton("View Full Report", (dialog, which) -> {
                    Toast.makeText(this, "Detailed insights coming soon!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showNotifications() {
        String[] notifications = {
                "12 new jobs match your profile!",
                "3 companies viewed your profile today",
                "Your profile completeness increased to " + profileCompleteness + "%",
                "Tip: Update your CV to boost visibility by 40%",
                "You're on a " + dailyStreak + " day streak! Keep it up!",
                "New skill assessment available: Digital Marketing"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Notifications")
                .setItems(notifications, (dialog, which) -> {
                    handleNotificationClick(which);
                })
                .setNegativeButton("Clear All", null)
                .show();
    }

    private void handleNotificationClick(int index) {
        switch (index) {
            case 0:
                navigateToJobsWithFilter("matches");
                break;
            case 1:
                showProfileInsights();
                break;
            case 2:
                showProfileCompletion();
                break;
            case 3:
                // Updated to go to tips page
                Intent cvIntent = new Intent(home.this, CvTipsActivity.class);
                startActivity(cvIntent);
                break;
            case 4:
                showAchievements();
                break;
            case 5:
                quickSkillAssessment();
                break;
        }
    }

    private void launchCareerAdvisorAI() {
        String advisorMsg = "AI Career Advisor\n\n" +
                "I'm your personal career coach! I can help you with:\n\n" +
                "• Job search strategies\n" +
                "• Resume optimization\n" +
                "• Interview preparation\n" +
                "• Career path planning\n" +
                "• Salary negotiations\n" +
                "• Skills development\n\n" +
                "What would you like help with today?";

        new MaterialAlertDialogBuilder(this)
                .setTitle("AI Career Advisor")
                .setMessage(advisorMsg)
                .setPositiveButton("Start Chat", (dialog, which) -> {
                    Toast.makeText(this, "AI Chat feature coming soon!", Toast.LENGTH_LONG).show();
                })
                .setNeutralButton("Quick Tips", (dialog, which) -> {
                    showQuickCareerTips();
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private void showProfileInsights() {
        String insightsMsg = "👤 Profile Insights\n\n" +
                "Profile Strength: " + profileCompleteness + "%\n" +
                "Profile Views (7 days): 23 (+15%)\n" +
                "Search Appearances: 45\n" +
                "Match Score: " + (profileCompleteness > 70 ? "Excellent ⭐⭐⭐" : "Good ⭐⭐") + "\n\n" +
                "Boost Your Profile:\n" +
                (profileCompleteness < 100 ? "• Complete missing sections\n" : "") +
                "• Add professional photo\n" +
                "• Get skill endorsements\n" +
                "• Update your CV regularly\n\n" +
                "Profiles with photos get 3x more views!";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Profile Insights")
                .setMessage(insightsMsg)
                .setPositiveButton("Edit Profile", (dialog, which) -> {
                    navigateToProfile();
                })
                .setNeutralButton("Complete Profile", (dialog, which) -> {
                    showProfileCompletion();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showProfileCompletion() {
        String[] sections = {
                (prefs.contains("user_name") ? "✅" : "⬜") + " Personal Information",
                (prefs.contains("user_phone") ? "✅" : "⬜") + " Contact Details",
                (prefs.contains("has_cv") ? "✅" : "⬜") + " Upload CV",
                (prefs.contains("experience_added") ? "✅" : "⬜") + " Work Experience",
                (prefs.contains("education_added") ? "✅" : "⬜") + " Education",
                "Professional Photo"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Complete Your Profile (" + profileCompleteness + "%)")
                .setMessage("Tap a section to complete it:")
                .setItems(sections, (dialog, which) -> {
                    Toast.makeText(this, "Opening " + sections[which].substring(2), Toast.LENGTH_SHORT).show();
                    navigateToProfile();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    // Helper navigation methods
    private void navigateToJobsWithFilter(String filter) {
        try {
            // If the filter is 'all' or 'matches', go to the new search page
            if ("all".equals(filter) || "matches".equals(filter) || "quick_apply".equals(filter)) {
                Intent intent = new Intent(this, JobSearchActivity.class);
                intent.putExtra("filter", filter); // The search page can optionally use this
                startActivity(intent);
            } else {
                // Otherwise, go to the map
                Intent intent = new Intent(this, nearme.class);
                intent.putExtra("filter", filter);
                startActivity(intent);
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Toast.makeText(this, "Opening jobs...", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToCV() {
        // This method is still here in case another part of the app calls it
        try {
            Intent intent = new Intent(this, managecv.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } catch (Exception e) {
            Toast.makeText(this, "Opening CV Builder...", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProfile() {
        try {
            // Assuming 'setting.class' is your main profile/settings page
            Intent intent = new Intent(this, setting.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } catch (Exception e) {
            Toast.makeText(this, "Opening Profile...", Toast.LENGTH_SHORT).show();
        }
    }


    private void navigateToSettings() {
        // This can also just point to the main settings/profile page
        navigateToProfile();
    }

    // Placeholder methods for future features
    private void showCoverLetterGenerator() {
        Toast.makeText(this, "AI Cover Letter Generator - Coming Soon!", Toast.LENGTH_LONG).show();
    }

    private void showPortfolio() {
        Toast.makeText(this, "Portfolio Showcase - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showAchievementHighlighter() {
        Toast.makeText(this, "Achievement Highlighter - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showCareerPath() {
        Toast.makeText(this, "Career Progression Path - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showMarketDemand() {
        Toast.makeText(this, "Market Demand Insights - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showCertificationPaths() {
        Toast.makeText(this, "Certification Paths - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void quickSkillAssessment() {
        Toast.makeText(this, "Starting Quick Assessment...", Toast.LENGTH_SHORT).show();
    }

    private void showSkillCourses() {
        Toast.makeText(this, "Recommended Courses - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showIndustryTrends() {
        Toast.makeText(this, "Industry Skill Trends - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showCompanyInsights() {
        Toast.makeText(this, "Company Research Hub - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showNetworkingHub() {
        Toast.makeText(this, "Networking Hub - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showSalaryCalculator() {
        Toast.makeText(this, "Salary Calculator - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void showQuickCareerTips() {
        String[] tips = getRandomCareerTips();
        String tipsMessage = "Quick Career Tips:\n\n" +
                "1. " + tips[0] + "\n\n" +
                "2. " + tips[1] + "\n\n" +
                "3. " + tips[2];

        new MaterialAlertDialogBuilder(this)
                .setTitle("Today's Career Tips")
                .setMessage(tipsMessage)
                .setPositiveButton("More Tips", (dialog, which) -> {
                    showQuickCareerTips();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private String[] getRandomCareerTips() {
        String[] allTips = {
                "Tailor your CV for each job application - it increases your chances by 50%",
                "Follow up 48 hours after an interview with a thank you email",
                "Use LinkedIn to research interviewers before your meeting",
                "Practice the STAR method for behavioral interview questions",
                "Network during your job search - 70% of jobs are never advertised",
                "Update your skills section monthly to match trending job requirements",
                "Create a 30-60-90 day plan to show initiative in interviews",
                "Research company culture through Glassdoor before applying",
                "Keep a 'brag document' of your achievements for easy CV updates",
                "Ask for informational interviews to learn about companies"
        };

        java.util.Random random = new java.util.Random();
        String[] selectedTips = new String[3];
        for (int i = 0; i < 3; i++) {
            selectedTips[i] = allTips[random.nextInt(allTips.length)];
        }
        return selectedTips;
    }
}