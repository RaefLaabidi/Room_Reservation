@echo off
echo.
echo ========================================
echo    FREE AI CHATBOT SETUP HELPER
echo ========================================
echo.

REM Check if Ollama is installed
where ollama >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Ollama not found. Please install it first:
    echo    1. Go to https://ollama.ai
    echo    2. Download for Windows
    echo    3. Install and restart this script
    echo.
    pause
    exit /b 1
)

echo ✅ Ollama is installed!
echo.

REM Check Ollama version
echo 📋 Checking Ollama version...
ollama --version
echo.

REM Pull the AI model
echo 📥 Downloading AI model (this may take a few minutes)...
echo    Model: llama3.2 (3GB download)
echo.
ollama pull llama3.2

REM Check if model was downloaded successfully
if %ERRORLEVEL% EQ 0 (
    echo.
    echo ✅ AI model downloaded successfully!
    echo.
) else (
    echo.
    echo ❌ Failed to download model. Check your internet connection.
    pause
    exit /b 1
)

REM Start Ollama service
echo 🚀 Starting Ollama service...
echo    This will start the AI server at http://localhost:11434
echo    Keep this window open while using the chatbot!
echo.
echo ✅ Your FREE AI is now ready!
echo    Start your reservation system and try the chatbot! 🤖
echo.
echo    Test questions:
echo    - "How many rooms do we have?"
echo    - "Show me today's bookings"  
echo    - "Generate a usage report"
echo.

REM Start the service (this will block)
ollama serve
