
# AndroidComponent

**组件化开发专题** :https://www.jianshu.com/c/22ffe717490b
 
1. **第1章 组件单独调试与集成发布** :https://www.jianshu.com/p/4b368ee8d4fd
提供插件支持 thorAlone

2. **第2章 组件化选型** :https://www.jianshu.com/p/4243b7c7f9be
提供组件化框架Arouter+auto-register+ThorComponent框架(正在忙碌自研中)

3. **第3章 组件声明式编程 仿微信".api"化** :https://www.jianshu.com/p/20108abc1dd6
提供插件支持 weixinApi 强力解决此问题

## 组件功能介绍

### app组件功能（空壳工程）：
1. 配置整个项目的Gradle脚本，例如 混淆、签名等；
2. app组件中可以初始化全局的库，例如Lib.init(this);
3. 添加 multiDex 功能
4. 业务组件管理（组装）；

### main组件功能（业务组件）：
1. 声明应用的launcherActivity----->android.intent.category.LAUNCHER；
2. 添加SplashActivity;
3. 添加LoginActivity；
4. 添加MainActivity；

### girls/news组件功能（业务组件）：
1. 这两个组件都是业务组件，根据产品的业务逻辑独立成一个组件；

### common组件功能（功能组件）：
1. common组件是基础库，添加一些公用的类；
2. 例如：网络请求、图片加载、工具类、base类等等；
3. 声明APP需要的uses-permission；
4. 定义全局通用的主题（Theme）；

## Thanks
    thanks guiying712 
    links:
    Android项目组件化示例代码
    **Android组件化方案**：http://blog.csdn.net/guiying712/article/details/55213884
    **Android组件化之终极方案**：http://blog.csdn.net/guiying712/article/details/78057120

## License

    Copyright 2019 yinglingchaoliu, AndroidComponent Open Source Project

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.