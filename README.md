# CrossApp Memory

> **Remember text across apps on your phone and turn it into searchable memories, intelligent Q&A answers, smart summaries, activity stats, and personal insights — saved locally and exportable as standard CSV files.**

---

## 🌟 Key Features

1. **📱 Multi-Source Cross-App Memory Capture**:
   - **Real-Time Accessibility Service**: Monitors focused text across browser articles, chat threads, note apps, and feeds in the background.
   - **Notification Listener**: Seamlessly captures incoming notification texts from messaging and social apps.
   - **Context Menu Text Processor**: Highlight text in *any* Android app and tap **"Save to Memory"**.
   - **Share Sheet Receiver**: Share webpages, notes, or messages directly via Android's native share sheet.
   - **Quick Capture FAB**: Fast manual logging and clipboard paste.

2. **🕒 Chronological Searchable Memory Timeline**:
   - Grouped chronologically by **Today**, **Yesterday**, **This Week**, **Earlier this Month**, and **Older**.
   - Instant full-text search across titles, remembered texts, source apps, and tags.
   - Filter chips for specific apps (Chrome, Slack, WhatsApp, Kindle, etc.) and categories.
   - Star high-value memories for fast access.
   - Add chronological **Addendums & Notes** to any memory.

3. **🧠 Answers Engine (Ask Your Memories)**:
   - Ask natural language questions about your captured text (e.g. *"What did Alex say about the roadmap?"*, *"What were the key takeaways from the newsletter?"*, *"Find dinner reservations"*).
   - Generates structured answers with key findings and direct citations linking back to the exact source app and timestamp.

4. **📑 Executive Summaries Engine**:
   - Generates high-level summaries for **Today**, **Past 7 Days**, **Past 30 Days**, or **All Time**.
   - Highlights key takeaways, action items, and app activity breakdowns.
   - 1-click **Copy Summary** and **Share**.

5. **📊 Personal Insights & Stats Engine**:
   - Total memories logged, words remembered, and unique apps monitored.
   - **24-Hour Information Flow Heatmap**: Visualize what times of day you read and write most.
   - App distribution progress bars and category breakdown.
   - Recurring topic & hashtag trends.
   - Automated detection of **Action Items & Tasks**.

6. **📁 Local Storage & RFC-4180 CSV Export**:
   - 100% offline local SQLite persistence via **Android Room**.
   - One-tap export to standard RFC-4180 `.csv` file via Android `FileProvider` with full metadata (`ID`, `Timestamp_Formatted`, `Timestamp_ISO`, `App_Name`, `Category`, `Title`, `Source_Type`, `Sentiment`, `Tags`, `Text_Content`, `Addendums`).
   - Easily open in Excel, Google Sheets, Pandas, or local AI fine-tuning pipelines.

7. **🔒 100% Local-First Privacy**:
   - Zero telemetry, zero cloud lock-in.
   - Automatic filtering of password, PIN, and sensitive input fields.

8. **🚀 GitHub Actions CI/CD Pipeline**:
   - Fully configured `.github/workflows/build.yml` and `.github/workflows/android.yml` utilizing `actions/setup-java@v5` and `actions/setup-node` with Node.js 24 runtime environment.
   - Automatically builds, tests, and packages debug and release APK artifacts on GitHub.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2+ (JVM 21)
- **UI Framework**: Jetpack Compose (Material 3 Dark/Light Theming)
- **Architecture**: MVVM + Clean Architecture + Repository Pattern
- **Local Persistence**: Room Database (KSP) + TypeConverters
- **Reactive State**: Kotlin Coroutines (`StateFlow`, `combine`, `SupervisorJob`)
- **System Services**: Android `AccessibilityService`, `NotificationListenerService`, `ProcessTextActivity`
- **Export Engine**: RFC-4180 CSV Generator + AndroidX `FileProvider`

---

## 📦 Building and Packaging on GitHub Actions

This repository includes continuous integration workflows:

1. Push your repository to GitHub:
   ```bash
   git add .
   git commit -m "Initialize CrossApp Memory"
   git push origin main
   ```
2. Navigate to **Actions** in your GitHub repository.
3. The **Android CI & Build Package** workflow will run tests and generate a downloadable `CrossAppMemory-Debug-APK` artifact.
