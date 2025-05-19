README



Variable names (make notepad full screen **)


| Screen            | EditTexts / Inputs                        | Buttons                                             | TextViews                 |
|-------------------|-------------------------------------------|-----------------------------------------------------|---------------------------|
| Login             | etUsername, etPassword                    | btnLogin, btnRegister, btnGoogleSignIn              | tvForgotPassword          |
| Register          | etFullName, etEmail, etPassword, etConfirmPassword | btnRegister                                | tvLoginRedirect           |
| Main Menu         | (N/A)                                     | btnAddCategory, btnAddExpense, btnViewEntries, btnViewSummary | (N/A)             |
| Add Category      | etCategoryName                            | btnSaveCategory                                     | (N/A)                     |
| Add Expense       | etExpenseTitle, etExpenseAmount           | btnSaveExpense, btnTakePhoto                        | tvSelectedPhotoPath       |
| View Entries      | (N/A)                                     | (N/A)                                               | (RecyclerView) rvEntries  |
| View Summary      | (N/A)                                     | (N/A)                                               | tvSummaryInfo             |
| Set Budget Goals  | etMinGoal, etMaxGoal                      | btnSaveGoals                                        | (N/A)                     |







Project Structure so far (26/04/2025)


PocketSafe/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/pocketsafe/
│   │   │   │       ├── data/
│   │   │   │       │   ├── AppDatabase.kt
│   │   │   │       │   ├── User.kt
│   │   │   │       │   ├── UserDao.kt
│   │   │   │       │   ├── UserDatabase.kt
│   │   │   │       │   ├── Expense.kt
│   │   │   │       │   ├── ExpenseDao.kt
│   │   │   │       │   ├── Category.kt
│   │   │   │       │   ├── CategoryDao.kt
│   │   │   │       │   ├── BudgetGoal.kt
│   │   │   │       │   ├── Account.kt
│   │   │   │       │   ├── AccountDao.kt
│   │   │   │       ├── ui/
│   │   │   │       │   ├── LoginActivity.kt
│   │   │   │       │   ├── RegisterActivity.kt
│   │   │   │       │   ├── MainMenu.kt
│   │   │   │       │   ├── CategoryActivity.kt
│   │   │   │       │   ├── ExpenseEntryActivity.kt
│   │   │   │       │   ├── ViewEntriesActivity.kt
│   │   │   │       │   ├── ViewSummaryActivity.kt
│   │   │   │       │   └── BudgetGoalActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_login.xml
│   │   │   │   │   ├── activity_register.xml
│   │   │   │   │   ├── activity_main_menu.xml
│   │   │   │   │   ├── activity_category.xml
│   │   │   │   │   ├── activity_expense.xml
│   │   │   │   │   ├── activity_view_entries.xml
│   │   │   │   │   ├── activity_view_summary.xml
│   │   │   │   │   ├── activity_budget_goal.xml
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── logo.png
│   │   │   │   │   ├── curved_background.xml
│   │   │   │   │   ├── curved_button.xml
│   │   │   │   ├── font/
│   │   │   │   │   ├── pixel_game.otf
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── themes.xml
│   │   │   │   │   ├── colors.xml
│   │   │   ├── AndroidManifest.xml
├── build.gradle (Project)
├── build.gradle (App)
├── README.md
