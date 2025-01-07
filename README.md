# Backpack
Assistant to help you dissect and store information from videos, podcasts, texts and more.

To get cookies:
```
yt-dlp -j --cookies-from-browser "firefox:/home/saleksandar/.mozilla/firefox/ry4b7uob.default-release/" --cookies cookies.txt https://www.youtube.com/watch?v=lAjVuUB9AkI
```

To download via yt-dlp:
```
yt-dlp https://www.youtube.com/watch?v=lAjVuUB9AkI --extract-audio --audio-format mp3 --no-keep-video --write-info-json --write-sub -o great-art.mp3 --cookies cookies.txt
```

To urn a build of a module:
```
 ./gradlew backpack-ai-server:build -Dorg.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64
```