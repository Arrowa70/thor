# 2018.7.10
```aidl
游戏需求
坦克大战io游戏需求
玩法模式：
1.生存模式 
  用户通过链接进入生存模式的一个房间
  每次用户死亡，积分清零，可重新开始
  用户一开始，坦克拥有无敌盾（持续时间5s）


2.游戏元素：
  1).动态元素：
    坦克（玩家独自控制自己的坦克，可以移动和发射子弹，并可以通过吃道具来增强实力）
     ·坦克能力等级：
        1.血量上限 3个等级（可通过吃道具提升等级）  120，240，300
        2.移动速度 3个等级（可通过吃道具提升等级）  5，7，9（每s移动的单位）
        3.子弹威力 3个等级（）                      20，40，60
       炮弹的方向是任意方向（鼠标控制）

    ·坦克其他能力
        坦克的子弹有最大容量4
        填充子弹的时间（1发/1s）
    子弹（有最长的飞行距离--80，）
  2).障碍物（静态物体）：
    砖头（障碍物，可被子弹击碎），
    空投箱（破坏后会随机掉落道具），
    【钢铁之后版本再添加】


  3).环境元素：
    【河流会阻碍坦克移动】

  4).游戏道具元素：
    增加血量上限，
    增加移动速度，
    提高子弹威力
    医疗包（+60的血量）
    【增加2个发射器，隐身等）】   


3.击杀奖励：击杀敌方坦克，会随机掉落道具在敌方坦克附件周围，当对方坦克吃的东西越多，掉的随机道具数量概率越多。

4.用户（坦克）死亡条件：用户坦克生命值<0

5.排行榜：击杀人数，伤害量（排序）
```