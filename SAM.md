## Dev
```shell script
sam local start-api -t build\sam.jvm.yaml
```

## First deploy
```shell script
.\gradlew.bat clean build
sam deploy --guided -t build\sam.jvm.yaml
```

## Next deploy
```shell script
.\gradlew.bat clean build
sam deploy
```