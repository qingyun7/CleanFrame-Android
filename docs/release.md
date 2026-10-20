# Android 1.1.2 发布记录

2026-10-20。独立 Android 移植，基于 Nathan Clark 的 CleanFrame `fa537d1892b71d2de2bf94c24cbff3812e9f70d5`。

1.1.2 保留已在小米上使用的 1.1.1 界面、单色图标和 280ms 缓入缓出动画；公开 release 关闭 WebView 调试，移除未使用的 Tizen 清单，补全独立构建步骤、源码导出和来源记录。自行编译时使用本机生成的随机签名口令，不在公开源码中保存固定口令。

已完成的设备验证来自同一界面的 1.1.1：小米 MiTV-MFTP0、Android 9、WebView 66，验证显示、导航、启动、Home 返回、应用图标与动画中间位置及快速变向。没有重新测试电视冷启动。1.1.2 没有覆盖安装到原用户电视；其发布 APK 在本机完成编译与签名验证。

公开候选源码运行资源检查、公开字体哈希和常见凭据模式检查、18 项浏览器交互检查，并进行源码导出核验。浏览器检查使用合成应用列表，包含遥控导航、应用启动桥接、全部图标映射、失效快捷方式清理、发现失败时保留配置、壁纸保存和 720p 缩放。不会把上游 Samsung 版本的测试数量作为本次 Android 的验证结果。

公开源码和导出包排除签名私钥、`.local`、构建缓存、电视地址、用户配置、APK 备份与个人应用清理记录。三个字体文件使用上游公开字体哈希校验。检查有范围限制，不代表已验证所有设备或第三方权利。

Photos and glass materials retain their credited Unsplash terms. Font and service-mark notices remain beside the assets and in the root notices. The upstream project and this adaptation are independent of Xiaomi and the represented streaming services.
