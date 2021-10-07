# W03

1.代码原理

​	这个隐写术图的核心方法是位于SteganographyEncoder类的encode方法，这个方法实现了将一个编码好的以byte数组存放的class文件隐藏进一张图片中。

​	隐写的原理大致可以理解为将byte文件以01串的形式隐藏在一张图片的连续像素点上，隐藏方法是定点改变一个32位int表示的像素点某几位上的01值。

​	这个类有两个重要的变量，一个是bitsFromColor，另一个是mask。bitsFromColor的含义是一次能在32位int中隐藏的01串数量，它的取值可以为1，2，4，8。mask是通过bitsFromColor计算出来的，可以理解为一种掩码，即与mask进行与运算便可以是一个int的特定位数上的值被设为0.

​	encode的方法的具体执行过程如下：

​	对文件的的每一个byte，每次取出bitsFromColor大小的位数，并通过移位操作和与运算将这些位储存在一个像素点的特定位置，每个像素点可以储存3次，不断循环，直到文件全部被存进去。

​	对于decode算法的优化，我的做法是每次存byte前判断一下byte是否小于0，因为存入图片的byte数组中存放的都是char，而char的取值非负。

2.两张图片

隐藏了QuickSorter的图片

![example.QuickSorter](../example.QuickSorter.png)

隐藏了ShellSorter的图片

![avator](../example.ShellSorter.png)

3.录屏

实现了快速排序的Geezer

[![avator](https://asciinema.org/a/440052.svg)](https://asciinema.org/a/440052)



实现了希尔排序的Geezer

[![avator](https://asciinema.org/a/440053.svg)](https://asciinema.org/a/440053)

4.同学图片

我拿的是母舰同学（201250137）的图

![mj_sort](../example.QuickSorter_MJ.png)

![mj_BubbleSort](../example.BubbleSort_MJ.png)

这两张图在我这运行不正常，因为它用的jdk版本太高，是jdk16，我的是jdk11，我的虚拟机无法解析它的class文件。

