# Chat2QQ
Bukkit QQ群聊天插件

## 介绍
Chat2QQ 是一个基于[MiraiMC](https://github.com/DreamVoid/MiraiMC)的Bukkit插件，能够让你在Minecraft服务器上与QQ群的玩家聊天，也能在QQ群上与Minecraft服务器的玩家聊天。

## 下载
* [Github 发布页](https://github.com/DreamVoid/Chat2QQ/releases)
* [Gitee 发布页](https://gitee.com/dreamvoid/Chat2QQ/releases) (中国)
* [MCBBS](https://www.mcbbs.net/thread-1231009-1-1.html) (中国)

## 开始使用
* 下载插件，并将插件文件放入plugins文件夹
* 下载[MiraiMC](https://github.com/DreamVoid/MiraiMC)插件（如果尚未下载），并将插件文件放入plugins文件夹
* 启动服务端（如果尚未启动）或使用诸如PlugMan的插件加载插件
* 使用指令“**/mirai login <账号> <密码>**”登录你的机器人账号
* 调整插件的配置文件
* 以管理员或控制台身份输入指令“**/chat2qq reload**”
* 享受优雅的QQ机器人服务！

## 指令和权限
### 指令
| 命令 | 描述 | 权限 |
| ---------------------------- | ---------------------- | ---------- |
| /chat2qq reload  | 重新加载插件配置 | miraimc.command.chat2qq |
| /qchat <消息> | 向QQ群发送聊天消息 | miraimc.command.qchat |

### 权限
| 权限节点 | 描述 | 默认 |
| ---------------------------- | ---------------------- | ---------- |
| miraimc.command.chat2qq | 允许使用 /chat2qq | OP |
| miraimc.command.qchat | 允许使用 /qchat | YES |
