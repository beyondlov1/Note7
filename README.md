# Note7

### gradle 使用maven库
```
mavenLocal()配置maven的本地仓库后，gradle默认会按以下顺序去查找本地的仓库：USER_HOME/.m2/settings.xml >> M2_HOME/conf/settings.xml >> USER_HOME/.m2/repository
```
复制maven 目录的 setting >  ${USER_HOME}/.m2/  
参考(不一定能解决):  
https://www.jianshu.com/p/a5bad045da73  
https://blog.csdn.net/a386139471/article/details/107738615  

###  build  
mvn install git@github.com:beyondlov1/sardine-okhttp.git  
mvn install git@github.com:beyondlov1/jgit-lite.git  
mvn install git@github.com:beyondlov1/jgit-lite.git/jgit-utils  
mvn install git@github.com:beyondlov1/jgit-lite.git/jgit-delta  
mvn install git@github.com:beyondlov1/jgit-lite.git/jgit-main  
mvn install git@github.com:beyondlov1/Time-NLP.git  

#### android studio 下载gradle/sdk/ndk不畅:
windows 下不要开启https的proxy,  修改: C:\Users\beyond\.gradle 的 gradle.properties

#### 要使用 minSdk >= 26
因为26之后才可以使用 java File 的 api 来操作自己app的数据
