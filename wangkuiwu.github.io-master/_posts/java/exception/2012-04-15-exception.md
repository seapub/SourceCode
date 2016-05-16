---
layout: post
title: "Java异常(二) 《Effective Java》中关于异常处理的几条建议"
description: "java exception"
category: java
tags: [java]
date: 2012-04-15 09:01
---
 
> 本章是从《Effective Java》摘录整理出来的关于异常处理的几条建议。

> **目录**  
[第1条: 只针对不正常的情况才使用异常](#anchor1)  
[第2条: 对于可恢复的条件使用被检查的异常，对于程序错误使用运行时异常](#anchor2)  
[第3条: 避免不必要的使用被检查的异常](#anchor3)  
[第4条: 尽量使用标准的异常](#anchor4)  
[第5条: 抛出的异常要适合于相应的抽象](#anchor5)  
[第6条: 每个方法抛出的异常都要有文档](#anchor6)  
[第7条: 在细节消息中包含失败 -- 捕获消息](#anchor7)  
[第8条: 努力使失败保持原子性](#anchor8)  
[第9条: 不要忽略异常](#anchor9)  
 
它们对应原书中"第8章 异常"部分的第39-47条。


 
<a name="anchor1"></a>
# 第1条: 只针对不正常的情况才使用异常

建议：异常只应该被用于不正常的条件，它们永远不应该被用于正常的控制流。

通过比较下面的两份代码进行说明。

**代码1**

    try {
        int i=0;
        while (true) {
            arr[i]=0;
            i++;
        }
    } catch (IndexOutOfBoundsException e) {
    }

**代码2**

    for (int i=0; i<arr.length; i++) {
        arr[i]=0;
    }

两份代码的作用都是遍历arr数组，并设置数组中每一个元素的值为0。代码1的是通过异常来终止，看起来非常难懂，代码2是通过数组边界来终止。我们应该避免使用代码1这种方式，主要原因有三点：  
> 1. 异常机制的设计初衷是用于不正常的情况，所以很少会会JVM实现试图对它们的性能进行优化。所以，创建、抛出和捕获异常的开销是很昂贵的。  
2. 把代码放在try-catch中返回阻止了JVM实现本来可能要执行的某些特定的优化。  
3. 对数组进行遍历的标准模式并不会导致冗余的检查，有些现代的JVM实现会将它们优化掉。

实际上，基于异常的模式比标准模式要慢得多。测试代码如下：

    public class Advice1 {

        private static int[] arr = new int[]{1,2,3,4,5};
        private static int SIZE = 10000;

        public static void main(String[] args) {

            long s1 = System.currentTimeMillis();
            for (int i=0; i<SIZE; i++)
                endByRange(arr);
            long e1 = System.currentTimeMillis();
            System.out.println("endByRange time:"+(e1-s1)+"ms" );

            long s2 = System.currentTimeMillis();
            for (int i=0; i<SIZE; i++)
                endByException(arr);
            long e2 = System.currentTimeMillis();
            System.out.println("endByException time:"+(e2-s2)+"ms" );
        }

        // 遍历arr数组: 通过异常的方式
        private static void endByException(int[] arr) {
            try {
                int i=0;
                while (true) {
                    arr[i]=0;
                    i++;
                    //System.out.println("endByRange: arr["+i+"]="+arr[i]);
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }

        // 遍历arr数组: 通过边界的方式
        private static void endByRange(int[] arr) {
            for (int i=0; i<arr.length; i++) {
                arr[i]=0;
                //System.out.println("endByException: arr["+i+"]="+arr[i]);
            }
        }
    }

运行结果：

    endByRange time:8ms
    endByException time:16ms

结果说明：通过异常遍历的速度比普通方式遍历数组慢很多！

 
<a name="anchor2"></a>
# 第2条: 对于可恢复的条件使用被检查的异常，对于程序错误使用运行时异常

|      异常        |                说明                |
| ---------------- | ---------------------------------- |
| 运行时异常   | RuntimeException类及其子类都被称为运行时异常。 |
| 被检查的异常 | Exception类本身，以及Exception的子类中除了"运行时异常"之外的其它子类都属于被检查异常 |

它们的区别是：**Java编译器会对"被检查的异常"进行检查，而对"运行时异常"不会检查。**

也就是说，对于被检查的异常，要么通过throws进行声明抛出，要么通过try-catch进行捕获处理，否则不能通过编译。而对于运行时异常，倘若既"没有通过throws声明抛出它"，也"没有用try-catch语句捕获它"，还是会编译通过。当然，虽说Java编译器不会检查运行时异常，但是，我们同样可以通过throws对该异常进行说明，或通过try-catch进行捕获。

rithmeticException(例如，除数为0)，IndexOutOfBoundsException(例如，数组越界)等都属于运行时异常。对于这种异常，我们应该通过修改代码进行避免它的产生。而对于被检查的异常，则可以通过处理让程序恢复运行。例如，假设因为一个用户没有存储足够数量的前，所以他在企图在一个收费电话上进行呼叫就会失败；于是就将一个被检查异常抛出。

 
<a name="anchor3"></a>
# 第3条: 避免不必要的使用被检查的异常

"被检查的异常"是Java语言的一个很好的特性。与返回代码不同，"被检查的异常"会强迫程序员处理例外的条件，大大提高了程序的可靠性。

但是，过分使用被检查异常会使API用起来非常不方便。如果一个方法抛出一个或多个被检查的异常，那么调用该方法的代码则必须在一个或多个catch语句块中处理这些异常，或者必须通过throws声明抛出这些异常。 无论是通过catch处理，还是通过throws声明抛出，都给程序员添加了不可忽略的负担。

适用于"被检查的异常"必须同时满足两个条件：第一，即使正确使用API并不能阻止异常条件的发生。第二，一旦产生了异常，使用API的程序员可以采取有用的动作对程序进行处理。

 
<a name="anchor4"></a>
# 第4条: 尽量使用标准的异常

代码重用是值得提倡的，这是一条通用规则，异常也不例外。重用现有的异常有几个好处：  
第一，它使得你的API更加易于学习和使用，因为它与程序员原来已经熟悉的习惯用法是一致的。  
第二，对于用到这些API的程序而言，它们的可读性更好，因为它们不会充斥着程序员不熟悉的异常。  
第三，异常类越少，意味着内存占用越小，并且转载这些类的时间开销也越小。

Java标准异常中有几个是经常被使用的异常。如下表格：

|      异常    |              使用场合              |
| ------------ | ---------------------------------- |
| IllegalArgumentException  | 参数的值不合适 |
| IllegalStateException | 参数的状态不合适 |
| NullPointerException | 在null被禁止的情况下参数值为null |
| IndexOutOfBoundsException | 下标越界 |
| ConcurrentModificationException | 在禁止并发修改的情况下，对象检测到并发修改  |
| UnsupportedOperationException | 对象不支持客户请求的方法 |

虽然它们是Java平台库迄今为止最常被重用的异常，但是，在许可的条件下，其它的异常也可以被重用。例如，如果你要实现诸如复数或者矩阵之类的算术对象，那么重用ArithmeticException和NumberFormatException将是非常合适的。如果一个异常满足你的需要，则不要犹豫，使用就可以，不过你一定要确保抛出异常的条件与该异常的文档中描述的条件一致。这种重用必须建立在语义的基础上，而不是名字的基础上！

最后，一定要清楚，选择重用哪一种异常并没有必须遵循的规则。例如，考虑纸牌对象的情形，假设有一个用于发牌操作的方法，它的参数(handSize)是发一手牌的纸牌张数。假设调用者在这个参数中传递的值大于整副牌的剩余张数。那么这种情形既可以被解释为IllegalArgumentException(handSize的值太大)，也可以被解释为IllegalStateException(相对客户的请求而言，纸牌对象的纸牌太少)。

 
<a name="anchor5"></a>
# 第5条: 抛出的异常要适合于相应的抽象

如果一个方法抛出的异常与它执行的任务没有明显的关联关系，这种情形会让人不知所措。当一个方法传递一个由低层抽象抛出的异常时，往往会发生这种情况。这种情况发生时，不仅让人困惑，而且也"污染"了高层API。

为了避免这个问题，高层实现应该捕获低层的异常，同时抛出一个可以按照高层抽象进行介绍的异常。这种做法被称为"异常转译(exception translation)"。

例如，在Java的集合框架AbstractSequentialList的get()方法如下(基于JDK1.7.0_40)：

    public E get(int index) {
        try {
            return listIterator(index).next();
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }

listIterator(index)会返回ListIterator对象，调用该对象的next()方法可能会抛出NoSuchElementException异常。而在get()方法中，抛出NoSuchElementException异常会让人感到困惑。所以，get()对NoSuchElementException进行了捕获，并抛出了IndexOutOfBoundsException异常。即，相当于将NoSuchElementException转译成了IndexOutOfBoundsException异常。

 
<a name="anchor6"></a>
# 第6条: 每个方法抛出的异常都要有文档

要单独的声明被检查的异常，并且利用Javadoc的@throws标记，准确地记录下每个异常被抛出的条件。

如果一个类中的许多方法处于同样的原因而抛出同一个异常，那么在该类的文档注释中对这个异常做文档，而不是为每个方法单独做文档，这是可以接受的。

 
<a name="anchor7"></a>
# 第7条: 在细节消息中包含失败 -- 捕获消息

简而言之，当我们自定义异常或者抛出异常时，应该包含失败相关的信息。

当一个程序由于一个未被捕获的异常而失败的时候，系统会自动打印出该异常的栈轨迹。在栈轨迹中包含该异常的字符串表示。典型情况下它包含该异常类的类名，以及紧随其后的细节消息。

 
<a name="anchor8"></a>
# 第8条: 努力使失败保持原子性

当一个对象抛出一个异常之后，我们总期望这个对象仍然保持在一种定义良好的可用状态之中。对于被检查的异常而言，这尤为重要，因为调用者通常期望从被检查的异常中恢复过来。

一般而言，一个失败的方法调用应该保持使对象保持在"它在被调用之前的状态"。具有这种属性的方法被称为具有"失败原子性(failure atomic)"。可以理解为，失败了还保持着原子性。对象保持"失败原子性"的方式有几种：

(01) 设计一个非可变对象。

(02) 对于在可变对象上执行操作的方法，获得"失败原子性"的最常见方法是，在执行操作之前检查参数的有效性。如下(Stack.java中的pop方法)：

    public Object pop() {
        if (size==0)
            throw new EmptyStackException();
        Object result = elements[--size];
        elements[size] = null;
        return result;
    }

(03) 与上一种方法类似，可以对计算处理过程调整顺序，使得任何可能会失败的计算部分都发生在对象状态被修改之前。

(04) 编写一段恢复代码，由它来解释操作过程中发生的失败，以及使对象回滚到操作开始之前的状态上。

(05) 在对象的一份临时拷贝上执行操作，当操作完成之后再把临时拷贝中的结果复制给原来的对象。

虽然"保持对象的失败原子性"是期望目标，但它并不总是可以做得到。例如，如果多个线程企图在没有适当的同步机制的情况下，并发的访问一个对象，那么该对象就有可能被留在不一致的状态中。

即使在可以实现"失败原子性"的场合，它也不是总被期望的。对于某些操作，它会显著的增加开销或者复杂性。  
总的规则是：作为方法规范的一部分，任何一个异常都不应该改变对象调用该方法之前的状态，如果这条规则被违反，则API文档中应该清楚的指明对象将会处于什么样的状态。

 
<a name="anchor9"></a>
# 第9条: 不要忽略异常

当一个API的设计者声明一个方法会抛出某个异常的时候，他们正在试图说明某些事情。所以，请不要忽略它！忽略异常的代码如下：

    try {
        ...
    } catch (SomeException e) {
    }

空的catch块会使异常达不到应有的目的，异常的目的是强迫你处理不正常的条件。忽略一个异常，就如同忽略一个火警信号一样 -- 若把火警信号器关闭了，那么当真正的火灾发生时，就没有人看到火警信号了。所以，至少catch块应该包含一条说明，用来解释为什么忽略这个异常是合适的。


