## 如何使用Xposed进行inlinHook Native 

思路分析->

1.利用xposed hook  so load 函数加载
2.植入hook so 链接到目标程序
3.hook so 进行 inlineHook

