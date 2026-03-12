# 🚀 How to Open PowerShell in Project Directory

## Method 1: Using Windows Explorer (Easiest) ⭐

1. **Open File Explorer** (Windows Key + E)
2. **Navigate to**: `C:\Users\shub\Downloads\kalyan-kosh-api`
3. **Click in the address bar** at the top (where it shows the path)
4. **Type**: `powershell` and press **Enter**
5. **Done!** PowerShell will open in that directory

![Visual Guide]
```
File Explorer → Address Bar → Type "powershell" → Enter
```

---

## Method 2: Right-Click Method (Windows 11)

1. **Open File Explorer** and go to: `C:\Users\shub\Downloads\kalyan-kosh-api`
2. **Right-click** on empty space in the folder
3. **Select** "Open in Terminal" or "Open PowerShell window here"
4. **Done!**

---

## Method 3: From Start Menu

1. **Press Windows Key**
2. **Type**: `powershell`
3. **Right-click** on "Windows PowerShell"
4. **Select** "Run as Administrator" (optional)
5. **Type this command**:
   ```powershell
   cd C:\Users\shub\Downloads\kalyan-kosh-api
   ```
6. **Press Enter**

---

## Method 4: Quick Command (Copy & Paste) ⚡

**Press Windows Key + R**, then copy and paste this:
```
powershell -NoExit -Command "cd 'C:\Users\shub\Downloads\kalyan-kosh-api'"
```
Press **Enter**

---

## Method 5: Use the Shortcut File I Created ⭐⭐⭐

I've created a file called **`OPEN_POWERSHELL_HERE.cmd`** in your project folder.

**Just double-click it!** It will:
- Open PowerShell
- Navigate to the project directory automatically
- You're ready to run commands!

---

## Verify You're in the Right Directory

After opening PowerShell, you should see:
```
PS C:\Users\shub\Downloads\kalyan-kosh-api>
```

To verify, type:
```powershell
Get-Location
```

You should see:
```
Path
----
C:\Users\shub\Downloads\kalyan-kosh-api
```

---

## Now What? Run These Commands:

Once PowerShell is open in the project directory:

### Start the Application:
```powershell
.\start-app.ps1
```

### Check Status:
```powershell
.\check-status.ps1
```

### Stop Application:
```powershell
.\stop-app.ps1
```

### Test API:
```powershell
.\test-api.ps1
```

---

## Troubleshooting

### If you see "script execution is disabled"
Run this command first:
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

Then try running the scripts again.

---

## Quick Video Guide (Text Version)

1. Press **Windows Key + E** (opens File Explorer)
2. Click the **address bar** at the top
3. Type **powershell**
4. Press **Enter**
5. You're now in PowerShell in your project directory! ✅

---

**That's it! Now you're ready to run the application!** 🎉

