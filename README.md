## 安裝方式

```
nvm use 20
npm i
```

## ios執行方式

```
PRODUCT=bw1 npx cap sync
PRODUCT=bw1 npx cap open ios
```
會打開xcode, 選擇相對應的scheme後點擊執行

## android執行方式

```
PRODUCT=bw1 npx cap sync
PRODUCT=bw1 npx cap open android
```

## Bitrise build android注意事項

設定好product flavor後，要在這邊

<img width="747" alt="Screenshot 2025-06-18 at 3 56 00 PM" src="https://github.com/user-attachments/assets/6ec99189-e432-4cb7-a48d-ab5f6eeded14" />

設定variant 為 <app代號>Release，這樣他就會去抓相對應的flavor, 不然都會是預設的，無法覆蓋appid等資料
