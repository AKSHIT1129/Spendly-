# Spendly

Spendly is a native Android application designed for personal and shared finance management. Developed in Android Studio, it provides a secure, local-first database architecture to track expenses, budgets, and savings goals for individuals, families, or small groups.

---

## Key Features

* **Multi-Member Profile Management**: Track individual and shared finances by setting up custom, color-coded profiles for up to 10 household members or roommates.
* **Category-Based Budgets**: Define strict monthly limits for spending categories (such as Rent, Food, or Entertainment) to monitor consumption and prevent overspending.
* **Savings Vaults**: Set financial target goals and visually track deposits and progress bars towards completion.
* **Upcoming Bill Reminders**: Track utility and subscription payments with due-date listings, togglable payment statuses, and alert simulations.
* **Dynamic Currency Conversion**: Instantly switch the active currency between INR, USD, and EUR, converting all dashboards and reports using real-time mock exchange rates.
* **Financial Analytics**: View graphical reports of category spending distributions and member contributions.

---

## Technical Specifications

* **Development Environment**: Android Studio (Koala or higher recommended)
* **Programming Language**: Kotlin
* **UI Toolkit**: Jetpack Compose and Material Design 3
* **Database**: Room Persistence Library (SQLite)
* **Architecture Pattern**: MVVM (Model-View-ViewModel) with repository caching and Kotlin StateFlow

---

## Installation and Setup

### Prerequisites
* Android Studio installed on your computer.
* JDK 17 (pre-packaged with modern Android Studio).
* An Android device running Android 7.0 (API Level 24) or higher, or a configured Android Virtual Device (Emulator).

### Step-by-Step Instructions

1. **Clone the Repository**
   Clone the repository or extract the project ZIP archive into a workspace folder:
   ```bash
   git clone https://github.com/AKSHIT1129/SPENDLY-V1.1.git
   ```

2. **Open in Android Studio**
   * Launch Android Studio.
   * Go to **File > Open** and select the folder containing the project files.
   * Allow Android Studio to automatically sync the Gradle files and resolve dependencies.

3. **Configure Environment File**
   * Copy the `.env.example` file in the root directory and rename the copy to `.env`.
   * Set the environment properties inside this file as needed for build preferences.

4. **Adjust Signing Configuration (Optional)**
   If you do not have the custom keystore setup, you can configure the build to use your machine's default debug keystore:
   * Open `app/build.gradle.kts` and navigate to the `buildTypes` block.
   * Comment out or delete the following line:
     ```kotlin
     signingConfig = signingConfigs.getByName("debugConfig")
     ```
   * Click **Sync Now** in the top-right notification bar to apply the changes.
---

## Deploying the Application (Running on a Physical Android Device)

1. Enable developer options on your Android device (Go to **Settings > About Phone** and tap **Build Number** 7 times).
2. Open **Developer Options** in Settings and enable **USB Debugging**.
3. Connect your device to the computer using a USB cable.
4. Select your connected device from the device dropdown menu at the top of Android Studio.
5. Click the green **Run** button (or press `Shift + F10`) to build and deploy the app.

## Running on an Emulator
1. Open the **Device Manager** in Android Studio.
2. Create and launch a virtual device (API Level 30+ recommended).
3. Select the emulator in the device selection menu at the top.
4. Click the green **Run** button to launch the application.

## Compiling a Standalone APK
1. In Android Studio, select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Locate the compiled file under:
   `[Project Directory]/app/build/outputs/apk/debug/app-debug.apk`
3. Transfer this file to your mobile device to install it manually.
