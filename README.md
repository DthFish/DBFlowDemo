# 我所了解的 DBFlow
***本篇文章已授权微信公众号 guolin_blog （郭霖）独家发布**

为啥要写数据库呢？公司的项目一直在进行着调整，整个项目的 module 已经超过 20，为了做到彻底解耦，我们的组的扛把子可谓是大刀阔斧，还多次向我表达了我们的 storage 模块需要调整的感慨。先说说现状，目前项目中使用的是 Ormlite，总体的感觉就是使用起来还是要写很多代码，看着之前的升级版本的逻辑头大。由于构建和升级的逻辑都在 storage 模块，所以要添加和修改表的话就要一定会修改到这个模块的代码，说白了还是耦合。

所以需求就是两点：
1. 使用简单，升级方便。
2. 多模块使用，让各个 module 负责各自的表。（这是不是就是一个 module 对应一个数据库来着）

上面两点是我学习了解数据库框架的目的，所以 DBFlow 也只是学习和尝试的框架之一，其他的后续再说。
### 简介
简单说明一下，本文使用的是 DBFlow 的 4.1.2 版，也是截止目前为止的最新版本，在 [github](https://github.com/Raizlabs/DBFlow) 上也可以看到，目前 DBFlow 还提供了一系列拓展，包括对 kotlin 的支持，Rxjava 的支持，数据库加密等。当然我是带着目的来的，所以我还是会主要关注上面提出来的两点需求，至于一些详细的使用还是尽力吧。

### 配置
为了后面多 Module 使用方便，先在项目中创建 config.gradle 文件，当然这个不是必须的。

~~~java
// config.gradle
ext {
    compileSdkVersion = 26
    buildToolsVersion = "26.0.2"
    minSdkVersion = 15
    targetSdkVersion = 26
    versionCode = 1
    versionName = "1.0"
    dbflow_version = "4.1.2"
}
~~~
对项目的 build.gradle 文件做以下修改：

~~~java
// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply from: "config.gradle" // 没有创建 config.gradle 就不是必须

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.0'
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://jitpack.io" } // 添加
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
~~~

在 app Module 中的 build.gradle 中添加依赖
~~~java
dependencies {
    // ...其他依赖
    annotationProcessor "com.github.Raizlabs.DBFlow:dbflow-processor:$dbflow_version"
    // gradle 3.0.0 可以使用 implementation，否则用 compile
    implementation "com.github.Raizlabs.DBFlow:dbflow-core:$dbflow_version"
    implementation "com.github.Raizlabs.DBFlow:dbflow:$dbflow_version"
}

~~~
到这里已经配置完毕了，下面我们开始愉快的创建数据库了。
### 创建数据库与表
新建 App 继承 Application，并在其中初始化：
~~~java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);// 初始化
    }
}
~~~
##### 新建数据库
~~~java
@Database(version = AppDatabase.VERSION)
public class AppDatabase {
    public static final int VERSION = 1;
}
~~~
##### 新建表
新建 Product 类，并用注解 **@Table** 标注，指定它的数据库为 AppDatabase；用 **@PrimaryKey** 标注 id 为主键，并且为自增长；用 **@Column** 标注 name 为表中的一列。
~~~java
@Table(database = AppDatabase.class)
public class Product extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
}
~~~

这里我们的表算是建完了，只需要点击 AndroidStudio 的 Build -> Make Project 之后，就可以在 app\build\generated\source\apt\debug 目录下找到生成的类 **Product_Table**，在里面可以看到一些 SQL 语句。
**注意：**这里我们继承的 **BaseModel** 是 DBFlow 给我们提供的，并不是我们自己项目中的，所以有人可能有疑问：我可不可以不继承它？答案是可以的，差别仅仅增删改查的操作上有所不同。

### CRUD
DBFlow 对数据的增删改查已经做了封装，使用起来比较简单，也不很容易理解。
##### Insert
对于向数据库插入数据的操作，对于已经继承了 BaseModel 的 bean，我们可以直接 new 一个出来，给相应的属性赋值之后，直接调用 save() 方法，数据就保存完毕了，代码如下。
另外，这里我们并没有给 Product 的主键 id 赋值，但是在保存完之后这个 id 就被赋值了。
~~~java
Product product = new Product();
product.name = "P" + (System.currentTimeMillis() % 10000);
product.save();
// 执行到这里之后 id 已经被赋值
~~~
##### Query
数据的查询，这里举了一个简单但是平时使用较为频繁的例子。从前文我们可以知道 **Product_Table** 是 apt 给我们生成的，使用起来也很灵活易懂，当然其他的更为复杂的建议大家可以查看一下官方文档。
~~~java
List<Product> products = SQLite.select()
	.from(Product.class)
	.where(Product_Table.name.isNotNull(), Product_Table.id.greaterThanOrEq(5L))// 这里的条件也可以多个
	.orderBy(Product_Table.id, true)// 按照 id 升序
	.limit(3)// 限制 3 条
	.queryList();// 返回的 list 不为 null，但是可能为 empty
~~~

##### Update
更新和删除可以为先查询后操作，只要查到对应的数据，在 bean 上做修改，然后调用 update() 方法，数据库就能修改完成。还有另一中更接近 sql 语法的方式。
~~~java
// 第一种 先查后改
Product product = SQLite.select()
		.from(Product.class)
		.querySingle();// 区别与 queryList()
if (product != null) {
    L.d("Update: " + product.name + " update to P0000");
    product.name = "P0000";
    product.update();
}
// 第二种
SQLite.update(Product.class)
		.set(Product_Table.name.eq("PXXXX"))
		.where(Product_Table.name.eq("P0000"))
		.execute();
~~~
##### Delete
与更新操作类似。
~~~java
// 第一种 先查后删
Product product = SQLite.select()
        .from(Product.class)
        .querySingle();
if (product != null) {
    product.delete();
    L.d("Delete: " + product.name);
}
// 第二种
SQLite.delete(Product.class)
		.where(Product_Table.name.eq("PXXXX"))
		.execute();
~~~
##### 对于未继承 BaseModel bean 的 CRUD
对于没有继承 BaseModel 的 bean，我们可以用以下方式进行数据操作，事实上前面的 save() 等方法最终也是通过这样处理的。当然定义表的时候的注解不可或缺。
这里，考虑到更新和删除的第二种方法，尝试了一下，果然还可以写出类似的插入方法。
~~~java
Product product = new Product();
product.name = "P" + (System.currentTimeMillis() % 10000);
FlowManager.getModelAdapter(Product.class).insert(product);
// 又一种插入方法
SQLite.insert(Product.class)
		.columnValues(Product_Table.name.eq("P" + (System.currentTimeMillis() % 10000)))
		.execute();
~~~
### 版本升级

##### 因新建表升级
好了现在我们已经清楚了 DBFlow 的基本使用了，但是一张表不能满足我们的需求啊！我还想创建一张 Category 表，那么赶紧再创建个类加下注解吧。
~~~java
@Table(database = AppDatabase.class)
public class Category extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
}
~~~
简单归简单，但是还是试一下增删改查功能吧！
~~~java
Category category = new Category();
category.name = "food";
category.save();
~~~
运行，之后就会发现了崩溃信息：
~~~java
 android.database.sqlite.SQLiteException: no such table: Category (code 1): ,
 while compiling: INSERT INTO `Category`(`name`) VALUES (?)
~~~
这里我们遗漏了数据库版本的升级，对于增加表格来说，DBFlow 版本升级其实很简单，我们只要找到我们的数据库类，并且把他的版本号加 1。
~~~java
@Database(version = AppDatabase.VERSION)
public class AppDatabase {
    public static final int VERSION = 2;
}
~~~
再重新运行下，我们就能在不影响 Product 表的前提下，成功新建了 Category 表了。

##### 因修改表结构升级
DBFlow 的表结构修改是通过 [Migration](https://github.com/agrosner/DBFlowDocs/blob/master/Migrations.md) 进行的，通过对它的实现，来进行对表的操作。
~~~java
public interface Migration {

    /**
     * Called before we migrate data. Instantiate migration data before releasing it in {@link #onPostMigrate()}
     * 在修改之前执行。
     */
    void onPreMigrate();

    /**
     * Perform your migrations here
     * 执行数据库操作
     * @param database The database to operate on 我们需要操作的数据库
     */
    void migrate(@NonNull DatabaseWrapper database);

    /**
     * Called after the migration completes. Release migration data here.
     * 在修改之后执行，释放资源
     */
    void onPostMigrate();
}
~~~
虽然看到这里，还是不知道怎么使用它，不过不用担心，DBFlow 已经有它的几个现成的实现提供给我们进行使用。
1. AlterTableMigration 用于重命名表，增加列
2. IndexMigration/IndexPropertyMigration 用于索引创建和删除
3. UpdateTableMigration 升级数据库的时候更新数据

下面我们就举一个相对常见的列子来看一下如何进行表结构的修改。
首先我们先修改一下之前创建的 Product 表：
~~~java
@Table(database = AppDatabase.class)
public class Product extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
    @Column(defaultValue = "10000")// 设置默认 100 块钱，即使忘了录入价格，我们卖出去也不吃亏（注意该属性坑爹）
    public long price;// 分
    @Column
    public String manufacturer;

}
~~~
这里我给加上了两列 price 和 manufactuer，然后希望价格默认为 100 元，接着写 Migration。
~~~java
@Migration(version = 3, database = AppDatabase.class)
public static class Migration3 extends AlterTableMigration<Product>{

    public Migration3(Class<Product> table) {
        super(table);
    }

    @Override
    public void onPreMigrate() {
        addColumn(SQLiteType.INTEGER, "price");
        addColumn(SQLiteType.TEXT, "manufacturer");
    }
}
~~~
1. 因为是添加表的列，所以继承 AlterTableMigration；
2. 修改 AppDatabase 的版本为 3，因为我们之前添加过 Category 表 version 为 2；
3. 添加注解，注解中 version 为现在的版本号 3，database 为我们的 AppDatabase 表；
4. 重写 onPreMigrate() 方法添加 addColumn() 就是我们在 Product 中新加的字段；

有疑问！如果一次升级我们不止改了一处，还有涉及到其他的修改咋办？
看到 **@Migration** 注解中有个 priority 这里我们姑且多建几个 Migration 用 priority 区分优先级来试试吧！
~~~java
@Migration(version = 3, priority = 2, database = AppDatabase.class)
public static class Migration3 extends AlterTableMigration<Product>{
	// 省略...
}
// 新建两个打上 log
@Migration(version = 3, priority = 0, database = AppDatabase.class)
public static class Migration3Zero extends BaseMigration {
    @Override
    public void onPreMigrate() {
    	L.d("Migration3Zero onPreMigrate: ");
    }
    @Override
    public void migrate(DatabaseWrapper database) {
    	L.d("Migration3Zero migrate: ");
    }
    @Override
    public void onPostMigrate() {
    	L.d("Migration3Zero onPostMigrate: ");
    }
}

@Migration(version = 3, priority = 1, database = AppDatabase.class)
public static class Migration3One extends BaseMigration {
    // 省略...
}

~~~
输出结果为：
>12-07 15:00:19.406 25213-25213/com.dthfish.dbflowdemo D/DBLog: Migration3Zero onPreMigrate:
12-07 15:00:19.406 25213-25213/com.dthfish.dbflowdemo D/DBLog: Migration3Zero migrate:
12-07 15:00:19.406 25213-25213/com.dthfish.dbflowdemo D/DBLog: Migration3Zero onPostMigrate:
12-07 15:00:19.406 25213-25213/com.dthfish.dbflowdemo D/DBLog: Migration3One onPreMigrate:
12-07 15:00:19.406 25213-25213/com.dthfish.dbflowdemo D/DBLog: Migration3One migrate:
12-07 15:00:19.406 25213-25213/com.dthfish.dbflowdemo D/DBLog: Migration3One onPostMigrate:

可以看到 priority 小的执行顺序优先，如果想要指定两个Migration 的 priority 相同的同学，就不要找不痛快了，因为没法保证执行顺序。
接下来看一下我们更新的表格内容：
1. 查询之前的数据，已经成功的添加了两个属性，但是 price = 0；
>12-07 15:00:19.426 25213-25213/com.dthfish.dbflowdemo D/DBLog:
>Query: [
>Product{id=6, name='P6396', price=0, manufacturer='null'},
>Product{id=7, name='P9297', price=0, manufacturer='null'},
>Product{id=8, name='P8988', price=0, manufacturer='null'},
>Product{id=9, name='P7232', price=0, manufacturer='null'},
>Product{id=10, name='P7147', price=0, manufacturer='null'},
>Product{id=11, name='P7634', price=0, manufacturer='null'}
>]

2. 新插入一条数据后，查询——然并软，**说好的 defaultValue 呢！你还我的 100 块！**
>12-07 15:06:47.666 25213-25213/com.dthfish.dbflowdemo D/DBLog:
>Query: [
>Product{id=6, name='P6396', price=0, manufacturer='null'},
>Product{id=7, name='P9297', price=0, manufacturer='null'},
>Product{id=8, name='P8988', price=0, manufacturer='null'},
Product{id=9, name='P7232', price=0, manufacturer='null'},
Product{id=10, name='P7147', price=0, manufacturer='null'},
Product{id=11, name='P7634', price=0, manufacturer='null'},
Product{id=12, name='P1172', price=0, manufacturer='null'}
]

##### 关于失效的的 defaultValue
好吧关于 defaultValue 失效我是始料未及的，在 [github](https://github.com/Raizlabs/DBFlow) 上的 Issues 中查看了一下，虽然有类似的问题但是还是没有找到正确的处理方法（希望不是我英文水平的问题），**如果有同学知道正确的方法请务必联系我！以免我误人子弟！**
但是我尝试出了我自己的方法。我的数据库要升级版本 4 啦！
~~~java
@Table(database = AppDatabase.class)
public class Product extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
    @Column//(defaultValue = "10000")// 设置默认 100 块钱，即使忘了录入价格，我们卖出去也不吃亏
    public long price = 10000L;// 分
    @Column
    public String manufacturer;
}

@Migration(version = 4, database = AppDatabase.class)
public static class Migration4 extends UpdateTableMigration<Product> {
    public Migration4(@NonNull Class<Product> table) {
        super(table);
    }
    @Override
    public void onPreMigrate() {
        where(Product_Table.price.eq(0L));
        set(Product_Table.price.eq(10000L));
    }
}
~~~
1. 去掉了 defaultValue 直接给 price 一个默认值；
2. 对版本 3 的补救措施，写了 Migration4，在 onPreMigrate 方法中对历史数据进行了处理。

##### 删掉表格的一列
见 github [Issue #467](https://github.com/Raizlabs/DBFlow/issues/467)
>This is a SQLite question, and short answer is...not easily. You have to (this is with SQLite too):
>1. Create new table without column with temporary name
>2. Copy over data to it
>3. Drop old table
>4. Recreate new table with column left out
>
>As I said, not easy.

1. 创建一个没有那一列的临时的表
2. 把数据复制进去
3. 删掉旧的表
4. 重新建个表

令我不禁想起了郭神的 [LitePal](https://github.com/LitePalFramework/LitePal)，对删除列做的良心处理。

### 多 module 使用
其实说了这么多，我最关心的还是多 module 的使用，毕竟我最初的目的还是这个。接下来的过程有些曲折，我会把过程中出现的错误以及处理方法都记录下来。

##### 创建多个 Module 添加依赖
这里除去 app，我又创建了 base，special，ship 三个 module，下面看一下他们的依赖配置。
~~~java
// base build.gradle
dependencies {
  	// 其他...
    annotationProcessor "com.github.Raizlabs.DBFlow:dbflow-processor:$dbflow_version"
    api "com.github.Raizlabs.DBFlow:dbflow-core:$dbflow_version"
    api "com.github.Raizlabs.DBFlow:dbflow:$dbflow_version"
}
// ship build.gradle
dependencies {
    // 其他...
    annotationProcessor "com.github.Raizlabs.DBFlow:dbflow-processor:$dbflow_version"
    api project(':base')
}
// special build.gradle
dependencies {
    // 其他...
    annotationProcessor "com.github.Raizlabs.DBFlow:dbflow-processor:$dbflow_version"
    api project(':base')
}
// app build.gradle
dependencies {
    // 其他...
    annotationProcessor "com.github.Raizlabs.DBFlow:dbflow-processor:$dbflow_version"
    implementation project(':ship')
    implementation project(':special')
}
~~~
这里不用在意 api 和 implementation，它们是 com.android.tools.build:gradle:3.0.0 才有的指令，统统可以改成为 compile。到这里我重新跑了一下程序，发现通过了，那还等什么赶紧试一下。
##### 在 ship module 中新建数据库和表
我没有把 AppDataBase 移到 base module 中，因为我希望各个 module 维护自己的表的时候不要修改到底层的 module，所以在 ship 中创建自己的 ShipDataBase。
~~~java
@Database(version = ShipDatabase.VERSION)
public class ShipDatabase {
    public static final int VERSION = 1;
}

@Table(database = ShipDatabase.class)
public class ShipProduct extends BaseModel{
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
}
~~~
##### 开始痛苦的解决问题
和前文一样，添加插入和查询方法，build 报错：
>Error:Error converting bytecode to dex:
Cause: com.android.dex.DexException: Multiple dex files define Lcom/raizlabs/android/dbflow/config/GeneratedDatabaseHolder;

What a f**k! 好吧强大的 google 指引我到 github [Issue #266](https://github.com/Raizlabs/DBFlow/issues/266)。
里面信息一堆，但是我隐约找到了我要的答案：
>What I learned is you can pass arguments to the processor. For example, the following addition to your build.gradle file >will pass the target module name to DBFlow-Compiler:
>
>~~~java
>apt {
>   arguments {
>       targetModuleName 'DBFlow'
>   }
>}
>~~~
>
>This will result in the database holder being named DBFlowGeneratedDatabaseHolder. It will be initialized as discussed >in previous comments.

另外还有人提出 apt 已经不维护了：
>I use
apt { arguments { targetModuleName 'DBFlow' } }
this solution in android studio 2.3.3
and that solves my problem but today I update my android studio to version 3.0 and we don't have apt or we cannot use it so any solution for this version of the android studio
I also ask the question in StackOverflow https://stackoverflow.com/questions/46998943/how-to-set-prefix-to-generateddatabaseholder-java-class-in-dbflow-in-android-stu

就是说，**在使用 apt(咋配置就不提了，大家可以查一下) 的情况下**，可以在 Ship 的 module 的 build.gradle 中添加：
~~~java
apply plugin: 'com.android.library'

android {
	// 省略...

}
apt {
    arguments{
        targetModuleName 'Ship'
    }
}
dependencies {
    // 省略...
}
~~~
**那么就可以解决问题，通过 build 的话，最终生成的类名会是 ShipGeneratedDatabaseHolder，与 app module 中的GeneratedDatabaseHolder 区别。**
但是现在的问题是 apt 已经不维护了，我们是否还能通过啥方法进行处理呢？
[Android注解使用之注解编译android-apt如何切换到annotationProcessor](https://www.cnblogs.com/whoislcj/p/6148410.html)
按照上文，我又修改了 Ship 的 build.gradle 文件：
~~~java
android {
    // 省略...
    defaultConfig {
        // 省略...
        jackOptions {
            enabled true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [ targetModuleName : 'Ship' ]
            }
        }

    }
	// 省略...
}
~~~
编译，报错：
![报错](pic\报错1.png)
好吧再改！AndroidStudio 告诉了我们解决方案：
~~~java
android {
    // 省略...
    defaultConfig {
        // 省略...
        android.compileOptions.sourceCompatibility 1.8
        android.compileOptions.targetCompatibility 1.8
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [ targetModuleName : 'Ship' ]
            }
        }

    }
	// 省略...
}
~~~
这次终于成功了跑起来了，我们试试插入吧！报错：（我已经习惯了）
~~~java
com.raizlabs.android.dbflow.structure.InvalidDBConfiguration:
Model object: com.dthfish.ship.database.ShipProduct is not registered with a Database. Did you forget an annotation?
~~~
还好，通过错误我们看到说我们没有在 Database 中注册，可是事实上我们已经加了 **@Table(database = ShipDatabase.class)** 注解。实际上，是我们新生成的 ShipGeneratedDatabaseHolder 需要注册。
~~~java
FlowConfig flowConfig = new FlowConfig.Builder(this)
        .addDatabaseHolder(ShipGeneratedDatabaseHolder.class)
        .build();
FlowManager.init(flowConfig);
~~~
到这里，多 module 的使用已经介绍完啦，我先去 Stackoverflow 上替外国友人解答下这个问题！

### 外键
终于到这里了，原来解决完上边的多 module 使用的问题其实已经感觉篇幅有点长了，但是考虑到外键的重要性觉得还是有必要讲一下的。

##### 升级数据库：给表添加外键
事实上一上来就会有疑问：**更新数据库的时候添加普通的一列和添加外键一样吗？**
对于我们的 bean 来说添加仅仅是添加了一个成员变量，但是更新数据库就不一样了，因为数据库里面这些自定义的数据结构是通过外键关联的。好吧我们又回到了数据库版本更新的问题，但是现在这个情况都不包含在之前提过的里面。下边看一下我最终尝试过后的结构：
~~~java
@Table(database = AppDatabase.class)
public class Product extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
    @Column//(defaultValue = "10000")// 设置默认 100 块钱，即使忘了录入价格，我们卖出去也不吃亏
    public long price = 10000L;// 分
    @Column
    public String manufacturer;
    @ForeignKey(stubbedRelationship = true, saveForeignKeyModel = true)
    public Category category;
}

@Migration(version = 5, database = AppDatabase.class)
public static class Migration5 extends AlterTableMigration<Product> {
    public Migration5(Class<Product> table) {
        super(table);
    }
    @Override
    public void onPreMigrate() {
        addForeignKeyColumn(SQLiteType.INTEGER, "category_id", FlowManager.getTableName(Category.class) +"(`id`) ");
    }
}
~~~
1. 在 Product 类中添加 category 字段，毕竟我们的产品是按品类划分的
2. 给 category 字段添加 **@ForeignKey** 注解，注解里面的参数，暂时先放一下，一会说
3. 编写 Migration5，修改版本号

虽然列了简单的三步但是还是有值得思考的地方：**onPreMigrate() 方法中的代码该怎么写？这个 category_id 哪里来的？**
事实上 addForeignKeyColumn() 方法是在我写**版本升级**的时候在 AlterTableMigration 类中发现的：
~~~java
/**
 * Add a column to the DB. This does not necessarily need to be reflected in the {@link TModel},
 * but it is recommended.
 *
 * @param sqLiteType      The type of column that pertains to an {@link SQLiteType}//添加的字段的类型
 * @param columnName      The name of the column to add. Use the "$Table" class for the specified table.//添加的列名
 * @param referenceClause The clause of the references that this foreign key points to.//外键的指向
 * @return This instance
 */
public AlterTableMigration<TModel> addForeignKeyColumn(SQLiteType sqLiteType, String columnName, String referenceClause) {
	// 省略...
}
~~~
虽然注释中有说各个参数的含义，但是事实上看了还是不知道怎么填写，SQLiteType 中只有一些基本的类型，最终给我提示的还是 apt 生成的 Product_Table 类。里面有这么一段：
~~~java
@Override
public final String getCreationQuery() {
    return "CREATE TABLE IF NOT EXISTS `Product`(`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT, `price` INTEGER, `manufacturer` TEXT, `category_id` INTEGER"+ ", FOREIGN KEY(`category_id`) REFERENCES " + com.raizlabs.android.dbflow.config.FlowManager.getTableName(com.dthfish.dbflowdemo.database.Category.class) + "(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION" + ");";
}
~~~
结果就是把里面的内容截取出来当参数了，运行结果也令人满意。**ps:如果这里有误请及时联系我，给我留言啊，我也是摸索出来的。**

##### 一对一外键
~~~java
public void foreignKeyInsert(View view) {
	// 为了保持每次的 Category 为同一个，其实是之前代码设计的错误，没有把 name 定为唯一
    Category category = SQLite.select()
            .from(Category.class)
            .where(Category_Table.name.eq("meat"))
            .querySingle();
    if (category == null) {
        category = new Category();
        category.name = "meat";
    }
    Product product = new Product();
    product.name = "P" + (System.currentTimeMillis() % 10000);
    product.category = category;
    product.save();
    L.d("Insert: " + product.toString());
    mLastInsertId = product.id;
}
private long mLastInsertId;
public void foreignKeyQuery(View view) {
	// 查询上次保存的 Product
    List<Product> products = SQLite.select()
            .from(Product.class)
            .where(Product_Table.id.eq(mLastInsertId))
            .queryList();
    L.d("Query: " + products.toString());
}
~~~
两个方法分别执行，得到：
~~~java
12-08 15:30:28.179 7615-7615/com.dthfish.dbflowdemo D/DBLog:
Insert: Product{id=6, name='P8158', price=10000, manufacturer='null', category=Category{id=1, name='meat'}}
12-08 15:30:30.419 7615-7615/com.dthfish.dbflowdemo D/DBLog:
Query: [Product{id=6, name='P8158', price=10000, manufacturer='null', category=Category{id=1, name='null'}}]
~~~
我们发现 query 操作查出来的 category 字段，只有 id，name 却等于 null;修改如下：
~~~java
private long mLastInsertId = 6L;// 6 为上边 log 打印出来的 id
public void foreignKeyQuery(View view) {
    List<Product> products = SQLite.select()
            .from(Product.class)
            .where(Product_Table.id.eq(mLastInsertId))
            .queryList();
    for (Product product : products) {
        product.category.load();
    }
    L.d("Query: " + products.toString());
}
~~~
结果：
~~~java
12-08 16:17:04.369 10814-10814/com.dthfish.dbflowdemo D/DBLog:
Query: [Product{id=6, name='P8158', price=10000, manufacturer='null', category=Category{id=1, name='meat'}}]
~~~
这里我们多执行了一步操作，调用了 Category 的 load() 方法，然后就查到了 id 为 1 的 Category 所有的信息了。
~~~java
@ForeignKey(stubbedRelationship = true, saveForeignKeyModel = true, deleteForeignKeyModel = false)
public Category category;
~~~
解释：
1. 我们标注 **@ForeignKey** 的时候声明了 stubbedRelationship = true，这样我们查询的时候会仅仅查出 category 的主键，当 load() 的时候才进一步查询；
2. 上面的例子当我们保存 Product 的时候，事实上也没有显性的调用 category.save()，最终查询的时候发现已经插入了 name = "meat" 的 Category，这是因为 saveForeignKeyModel = true。有兴趣的同学可以自己试一下 deleteForeignKeyModel = true;

##### 一对多
~~~java
@Table(database = AppDatabase.class)
public class Product extends BaseModel {
    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public String name;
    @Column//(defaultValue = "10000")// 设置默认 100 块钱，即使忘了录入价格，我们卖出去也不吃亏
    public long price = 10000L;// 分
    @Column
    public String manufacturer;
    @ForeignKey(stubbedRelationship = true, saveForeignKeyModel = true, deleteForeignKeyModel = false)
    public Category category;
    public List<Product> present;
    public List<Product> getPresent() {
        if (present == null || present.isEmpty()) {
            present = SQLite.select()
                    .from(Product.class)
                    .where(Product_Table.name.like("PX%"))
                    .queryList();
        }
        return present;
    }
}
~~~
简单的带过一下，就是添加一个 List，通过 get 方法去查询，仅此而已，甚至不需要升级版本号。
### 事务
### ContentProvider
剩下的几个主题就不再写了，有兴趣的同学可以自己看下官方文档。
### 最后
说实话在写这篇文章之前我自己写过另一个 Demo，但是那个就没有这篇文章举得例子这样简单明了。当然 DBFlow 的还有很多细节我没有讲到，包括但不限于加密，Rxjava这些的支持。正因为我自己在学习 DBFlow 的过程中也去找了相关文章，要么说的都比较简单，要么版本不是最新的，最后还是自己看文档进行尝试，总结一篇出来。结果也是有很多意外收获，总之在没有开始写的时候我知道篇幅会很长，但是实际上更长，所以最后有几个想讲的地方也就懒得完善了，哈哈！

如果有喜欢这篇文章的同学，请务必给我一个赞呀！这是对广大写博客的同学的最大的肯定！
[BDFlow github 地址](https://github.com/Raizlabs/DBFlow)
[Demo 地址](https://github.com/DthFish/DBFlowDemo)
[原文地址](http://www.jianshu.com/p/0c017a715410)




