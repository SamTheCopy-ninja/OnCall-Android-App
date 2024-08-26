# OnCall - Timesheet App

OnCall is an Android application that allows users to track their work hours, tasks, and goals efficiently.  
It provides a user-friendly interface for creating, managing, and organizing timesheet entries, categories, and work goals.  

Follow this link to view a video walkthrough showcasing the app: [OnCall demo video](https://youtu.be/W51ZEJHWky4)  

# Download the .apk file included in this repository if you wish to install and run the OnCall app on your mobile device  
- Please ensure your Android device settings allow you to run and install unknown apps, as the .apk file is currently unsigned and is meant for testing purposes
- Once the file is downloaded to your device, click on the .apk file to install it, the app should now open and run locally on your mobile device

## Getting Started

1. Open Android Studio and clone the repository: `https://github.com/SamTheCopy-ninja/OnCall-Android-App.git`
2. Run the Gradle build script, load script configurations and check that daemon file paths match, and sync the build.
3. Build and run the app on an emulator.  

## Change log and feature updates

This updated prototype introduces these new features:  
### Graphs to show Total Hours worked + Minimum/Maximum Hours worked  
- Users can now view graphs that show the total hours worked each day, based on their created Timesheets
- To monitor their progress over time, users can also view graphs for Minimum and Maximum Hour Work Goals which they have set
- Users can now filter graphs over a selectable period of time

### Marking Timesheet entries as "Completed"  
- To keep up with work tasks and timesheet entries, users are now able to mark any entry as `Completed`
- This provides users with more flexibility, as they can now choose to delete an entry or simply mark it as complete and keep it on their list of entries

### Pop-up Notification for scheduled entries  
- If the user creates an entry for the present day, or a future date, the app will keep track of this and display a reminder message about the Timesheet entry, when the user logs in on that particular date

### Users Leaderboard  
- This version of the app also introduces a leaderboard for all users
- The leaderboard displays the username and the number of entries the user has currently marked as `completed`
- This can be used to track individual progress, or see how the user ranks compared to other people using the app

### Logout functionality  
- This version of the app has also been updated to include logout functionality  


## Configuration Details

This app was built using `Android Studio Koala | 2024.1.1 Patch 2`.   
Please update your version if required, and ensure your `Android SDK` is also updated to the most recent version.   
This build uses `Android emulator v.34.2.16`
This app uses `Android Gradle plugin version 8.5.2`, please update your build if required. 

## Features

- **User Authentication**: Users can register and log in using Firebase Authentication.
- **Category Management**: Users can create and manage categories to tag their timesheet entries.
- **Timesheet Entry Creation**: Users can create timesheet entries with details such as task name, location, and optional photos.
- **Home Screen**: The home screen displays all existing timesheet entries for easy access and overview.
- **Work Goal Setting**: Users can set daily work goals by selecting a day of the week and specifying minimum and maximum targets.
- **Entry Deletion**: Users can delete timesheet entries.
- **Category Deletion and Renaming**: Users can delete or rename created categories.
- **Timesheet Entry Filtering**: Timesheet entries on the home screen can be filtered based on a specific date.  
  
- **Graphs for progress tracking**: Users can now view graphs displaying Total Hours Works, or their goals set for Minimum and Maximum work hours.
- **Graph Filtering**: Users can now filter graphs based on specific dates.
- **User Leaderboard**: Users can now compare their progress against other users, by viewing the leaderboard position based on the number of entries they complete.
- **Reminder Notifications**: When logging in, users will now be reminded if they have entries scheduled for that particular date.  

## Technologies Used

- Android Studio
- Kotlin
- Firebase Authentication
- Firebase Realtime Database  
- Firebase Storage

## References
  
This project adapts some code from this source:  
Author: Mkr Developer  
Source: [YouTube Playlist](https://www.youtube.com/watch?v=KiJy5Oi4rRo&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ)


## Author

Project maintainer: [Samkelo Tshabalala](https://github.com/ST10082747) from the 13th Coffin.
