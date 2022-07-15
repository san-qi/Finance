# Finance assistant based on kotlin

基于 CS 架构的财务助手 Android 客户端搭建,采用了Jetpack-compose编写界面,Room访问数据库,Datastore作为本地缓存访问用户首选项,Retrofit2请求网络资源

该项目作为完整项目的前端,rust后端请参见[这里](https://github.com/san-qi/Finance_server)

## 项目结构

```
.
├── FinanceNavHost.kt
├── MainActivity.kt
├── room
│   ├── Converts.kt
│   ├── FinanceDatabase.kt
│   └── store
│       ├── RecordDao.kt
│       └── Record.kt
├── Screen.kt
├── ui
│   ├── components
│   │   ├── AnimatedCircle.kt
│   │   ├── AnimatedTable.kt
│   │   ├── BezierSpline.java
│   │   ├── CommonUi.kt
│   │   ├── FinanceFAB.kt
│   │   ├── TabRow.kt
│   │   └── UserFormt.kt
│   ├── screen
│   │   ├── AccountScreen.kt
│   │   ├── ChangePasswordScreen.kt
│   │   ├── DetailsScreen.kt
│   │   ├── HelpScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── NewRecordScreen.kt
│   │   ├── OverviewScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── SettingsScreen.kt
│   │   └── UserCenterScreen.kt
│   └── theme
│       ├── Color.kt
│       ├── Shape.kt
│       ├── Theme.kt
│       └── Type.kt
└── utils
    ├── ApiService.kt
    ├── DataClass.kt
    └── DataStoreUtil.kt

```

- MainActivity.kt: 程序入口
- FinanceNavHost.kt: 导航
- Screen.kt: 屏幕界面枚举类型
- room: 数据库访问
- ui
  - components: 公共UI组件
  - screen: 屏幕显示内容主体
- utils
  - ApiService.kt: retrofit2 工具类
  - DataStoreUtil.kt: dataStore 工具类

## 实际展示
![gif](https://github.com/san-qi/Finance_client/blob/master/show.gif?raw=true)

## 灵感来源

该项目最初版本完善于[Rally](https://material.io/design/material-studies/rally.html),其中记录条目类型来源于一款APP--随手记,余下部分均由个人独立完成

## 授权许可

请遵循[GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)开源许可,上传到 github 仅仅是为了记录,这也算是大四学期的用心之作了
