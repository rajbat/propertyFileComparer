# propertyFileComparer
JavaFX-based property file comparer with search, diff, and export.

## ✨ Features

- 📂 Load two `.properties` files
- 🧪 Show differences side-by-side
- 🔍 Highlight missing or mismatched keys
- 🎚 Toggle between:
  - **All differences**
  - **Only missing keys**
  - **File1-only** / **File2-only** difference view
- 🔎 Search support within the table
- 💾 Export comparison result to file
- 🔁 Reload files easily
- ⚡ Handles large files (50K+ lines)

---

## 📦 Requirements

- Java 21
- Maven 3.6+
- JavaFX 21 SDK

---

## 🚀 Running the App

### 1. Build

```bash
mvn clean package javafx:run
