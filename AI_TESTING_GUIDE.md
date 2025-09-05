# 🤖 AI Chatbot Testing Guide

## 🚀 Quick Start - Test Your AI Now!

### **Step 1: Start Your Backend**
```powershell
cd backend
mvn spring-boot:run
```
Backend will start on: `http://localhost:8080`

### **Step 2: Start Your Frontend**  
```powershell
cd frontend
npm start
```
Frontend will open: `http://localhost:3000`

### **Step 3: Open the AI Chatbot**
1. Go to `http://localhost:3000` in your browser
2. Look for the **🤖 button** in the bottom-right corner
3. Click it to open the AI chat window

---

## 📡 Direct API Testing (Advanced)

### **Test AI via REST API:**
```powershell
# Basic test (works even without Ollama)
curl -X POST http://localhost:8080/api/chatbot/message `
  -H "Content-Type: application/json" `
  -d '{"message": "How many rooms do we have?"}'

# Check chatbot status
curl http://localhost:8080/api/chatbot/status

# Get suggested questions
curl http://localhost:8080/api/chatbot/suggestions
```

### **Example API Response:**
```json
{
  "message": "📊 **System Info**: We currently have 5 rooms in our system.",
  "timestamp": 1693859200000,
  "status": "success"
}
```

---

## 🎯 Test Questions for Your Professor

### **Basic System Info:**
- "How many rooms do we have?"
- "How many events are scheduled?"
- "How many courses are available?"
- "How many users are registered?"

### **Analytics Questions:**
- "Show me today's bookings"
- "Which room is most popular?"
- "Generate a usage report"
- "What are the peak hours?"

### **Smart Questions (with Ollama):**
- "Analyze our room utilization patterns"
- "Which rooms might be underutilized?"
- "What recommendations do you have for scheduling?"
- "Create a summary for administration"

---

## 🔧 Troubleshooting

### **Backend Not Starting:**
```powershell
# Check if port 8080 is free
netstat -an | findstr :8080

# Kill process using port 8080 if needed
taskkill /F /PID [PID_NUMBER]
```

### **API Not Responding:**
```powershell
# Test if backend is running
curl http://localhost:8080/api/chatbot/status
```

### **AI Responses Generic:**
- ✅ **With Ollama**: Intelligent, context-aware responses
- ⚠️ **Without Ollama**: Smart fallback using your real data
- Both work great for demos!

---

## 🎮 Demo Flow for Professor

### **1. Show the Interface:**
"Here's our AI-powered admin assistant integrated into our reservation system."

### **2. Ask Project-Specific Questions:**
```
👤 You: "How many rooms are in our system?"
🤖 AI: "In YOUR system, you have 5 rooms available for scheduling..."

👤 You: "Show me our current usage"  
🤖 AI: "Based on your current data, you have 12 scheduled events..."
```

### **3. Demonstrate Intelligence:**
```
👤 You: "Generate a summary report"
🤖 AI: "Here's your system analysis: 5 rooms, 12 events, 3 courses, peak usage..."
```

### **4. Highlight Technical Achievement:**
- "This AI analyzes our **real database** in real-time"
- "It provides **specific answers** about our system, not generic responses"
- "Uses **free Ollama AI** - no ongoing costs"
- "**Private and secure** - data never leaves our system"

---

## 📊 What Makes This Impressive

### **Technical Excellence:**
✅ **Real AI Integration** - Not just chatbot rules  
✅ **Live Data Access** - Connected to actual database  
✅ **Multi-Provider Support** - Ollama, OpenAI, fallback  
✅ **Professional UI** - Modern React chat interface  
✅ **REST API** - Can be integrated anywhere

### **Business Value:**
✅ **Natural Language Queries** - Ask questions in plain English  
✅ **Instant Analytics** - Get insights without complex queries  
✅ **Always Available** - 24/7 admin assistant  
✅ **Cost Effective** - Free AI with professional results

---

## 🚀 Quick Demo Commands

```powershell
# 1. Start everything
cd backend && mvn spring-boot:run &
cd frontend && npm start

# 2. Open browser
start http://localhost:3000

# 3. Click 🤖 button and ask:
# - "How many rooms do we have?"
# - "Show me system statistics" 
# - "Generate a usage report"
```

## 🎯 Success Indicators

✅ **Backend running**: `http://localhost:8080/api/chatbot/status` returns 200  
✅ **Frontend working**: `http://localhost:3000` loads your app  
✅ **AI responding**: 🤖 button opens chat and answers questions  
✅ **Smart answers**: AI gives specific numbers about YOUR system  

**Your AI chatbot is ready to impress! 🚀**
