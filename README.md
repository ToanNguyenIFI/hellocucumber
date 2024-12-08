# E2E Test Automation Framework

**1. Running Tests:**

mvn clean verify -Dcucumber.filter.tags=@SCRUM-3 or @SCRUM-9 -Dbrowser=chrome -DexecutingEnv=dev -DtestedEnv=dev -Dplatform=desktop

mvn clean verify -Dcucumber.filter.tags="@SCRUM-3 or @SCRUM-9" -Dbrowser=chrome -DexecutingEnv=dev -DtestedEnv=dev -Dplatform=desktop -Ddataproviderthreadcount=2


mvn clean verify -Dcucumber.filter.tags=@testAge -Dbrowser=chrome -DexecutingEnv=test -DtestedEnv=uat -Dplatform=ios-nativeApp

- In case testing web app in mobile

mvn clean verify -Dcucumber.filter.tags=@testAge -Dbrowser=chrome -DexecutingEnv=test -DtestedEnv=uat -Dplatform=android-webApp

mvn clean verify -Dcucumber.filter.tags=@testAge -Dbrowser=safari -DexecutingEnv=test -DtestedEnv=uat -Dplatform=ios-webApp


#### Using xray api to import Cucumber test from feature files

- Feature file scenario name must be equal Cucumber test summary

- Zip all feature folders

- Run command "curl" to update Cucumber test

curl -H "Content-Type: multipart/form-data" -X POST -H "Authorization: Bearer $token"  -F "file=@features.zip" https://xray.cloud.getxray.app/api/v2/import/feature?projectKey=TEST
