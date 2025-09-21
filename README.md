# Personalized Trip Planner with AI

A Spring Boot application that leverages AI to generate personalized travel itineraries, integrating health, safety, real-time data, and travel logistics to enhance the travel planning experience.

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Configuration & API Keys](#configuration--api-keys)

## Features
- **Personalized Itinerary Generation:** Uses an AI model to create a unique, day-by-day itinerary tailored to the user's interests and budget.
- **Health and Safety Integration:** Incorporates user-provided health data (allergies, medications, conditions) to provide relevant safety information.
- **Real-Time Data Integration:** Displays up-to-date information, including live weather forecasts for the destination city.
- **Travel & Route Planning:** Provides estimated travel distances and durations between source and destination.
- **Emergency Services Access:** Locates nearby hospitals and provides contact information for added peace of mind.

## Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.5.5
- **AI Model:** Ollama Phi3 (local deployment)
- **IDE:** IntelliJ IDEA
- **Other Tools:** Maven, Git

## Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/your-repo.git
   
2.	**Navigate to the project directory:**
     ```bash
     cd your-repo

3.	**Install dependencies:**
     ```bash
     mvn install
4.	**Install and run Ollama Phi3 model locally:**
    Follow Ollama’s official instructions to install and run the Phi3 model on your machine. Ensure it is accessible for your Spring Boot application.

## Running the Application
1.	**Start the Spring Boot application using IntelliJ IDEA:**
   - Open the project in IntelliJ IDEA.
   - Navigate to src/main/java/com/yourpackage/Application.java.
   - Run the application.

3.	**Access the application:**
      ```bash
         http://localhost:8080
3.	**The system will generate personalized itineraries based on user input and AI integration.**

## Configuration & API Keys
   The application requires several API keys to function properly. Add these keys to your application.properties or environment variables:
      
         # Route planning
         openrouteservice.api.key=<YourOpenRouteServiceAPIKey>
         
         # Weather data
         openweathermap.api.key=<YourOpenWeatherMapAPIKey>
         
         # Geocoding / Location data
         geoapify.api.key=<YourGeoapifyAPIKey>
         opencage.api.key=<YourOpenCageAPIKey>
         
         # AI Model
         ollama.api.key=<YourOllamaAPIKey>

Make sure each key is valid and active. Without these keys, the corresponding features (route planning, weather info, geocoding, AI itinerary generation) will not work.
