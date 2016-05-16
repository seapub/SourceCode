---
layout: post
title: "二叉查找树(二)之 C++语言详解"
description: "bubble sort"
category: datastructure
tags: [tree,cpp]
date: 2013-02-01 12:18
---

 
> 上一章介绍了"二叉查找树的相关理论知识，并通过C语言实现了二叉查找树"。这一章给出二叉查找树的C++版本。这里不再对树的相关概念进行介绍，若遇到不明白的概念，可以在上一章查找。

> **目录**  
[第1部分 二叉查找树简介](#anchor1)  
[第2部分 二叉查找树的C++实现](#anchor2)  
[第3部分 二叉查找树的C++实现(完整源码)](#anchor3)  
 
<a name="anchor1"></a>
# 第1部分 二叉查找树简介

二叉查找树(Binary Search Tree)，又被称为二叉搜索树。

它是特殊的二叉树：对于二叉树，假设x为二叉树中的任意一个结点，x节点包含关键字key，节点x的key值记为key[x]。如果y是x的左子树中的一个结点，则key[y] <= key[x]；如果y是x的右子树的一个结点，则key[y] >= key[x]。那么，这棵树就是二叉查找树。如下图所示：

在二叉查找树中：  
(01) 若任意节点的左子树不空，则左子树上所有结点的值均小于它的根结点的值；  
(02) 任意节点的右子树不空，则右子树上所有结点的值均大于它的根结点的值；  
(03) 任意节点的左、右子树也分别为二叉查找树。  
(04) 没有键值相等的节点（no duplicate nodes）。

 
<a name="anchor2"></a>
# 第2部分 二叉查找树的C++实现

## 1. 节点和二叉查找树的定义

### 1.1 二叉查找树节点

    template <class T>
    class BSTNode{
        public:
            T key;            // 关键字(键值)
            BSTNode *left;    // 左孩子
            BSTNode *right;    // 右孩子
            BSTNode *parent;// 父结点

            BSTNode(T value, BSTNode *p, BSTNode *l, BSTNode *r):
                key(value),parent(),left(l),right(r) {}
    };

BSTNode是二叉查找树的节点，它包含二叉查找树的几个基本信息：  
(01) key -- 它是关键字，是用来对二叉查找树的节点进行排序的。  
(02) left -- 它指向当前节点的左孩子。  
(03) right -- 它指向当前节点的右孩子。  
(04) parent -- 它指向当前节点的父结点。  

 

### 1.2 二叉树操作

    template <class T>
    class BSTree {
        private:
            BSTNode<T> *mRoot;    // 根结点

        public:
            BSTree();
            ~BSTree();

            // 前序遍历"二叉树"
            void preOrder();
            // 中序遍历"二叉树"
            void inOrder();
            // 后序遍历"二叉树"
            void postOrder();

            // (递归实现)查找"二叉树"中键值为key的节点
            BSTNode<T>* search(T key);
            // (非递归实现)查找"二叉树"中键值为key的节点
            BSTNode<T>* iterativeSearch(T key);

            // 查找最小结点：返回最小结点的键值。
            T minimum();
            // 查找最大结点：返回最大结点的键值。
            T maximum();

            // 找结点(x)的后继结点。即，查找"二叉树中数据值大于该结点"的"最小结点"。
            BSTNode<T>* successor(BSTNode<T> *x);
            // 找结点(x)的前驱结点。即，查找"二叉树中数据值小于该结点"的"最大结点"。
            BSTNode<T>* predecessor(BSTNode<T> *x);

            // 将结点(key为节点键值)插入到二叉树中
            void insert(T key);

            // 删除结点(key为节点键值)
            void remove(T key);

            // 销毁二叉树
            void destroy();

            // 打印二叉树
            void print();
        private:
            // 前序遍历"二叉树"
            void preOrder(BSTNode<T>* tree) const;
            // 中序遍历"二叉树"
            void inOrder(BSTNode<T>* tree) const;
            // 后序遍历"二叉树"
            void postOrder(BSTNode<T>* tree) const;

            // (递归实现)查找"二叉树x"中键值为key的节点
            BSTNode<T>* search(BSTNode<T>* x, T key) const;
            // (非递归实现)查找"二叉树x"中键值为key的节点
            BSTNode<T>* iterativeSearch(BSTNode<T>* x, T key) const;

            // 查找最小结点：返回tree为根结点的二叉树的最小结点。
            BSTNode<T>* minimum(BSTNode<T>* tree);
            // 查找最大结点：返回tree为根结点的二叉树的最大结点。
            BSTNode<T>* maximum(BSTNode<T>* tree);

            // 将结点(z)插入到二叉树(tree)中
            void insert(BSTNode<T>* &tree, BSTNode<T>* z);

            // 删除二叉树(tree)中的结点(z)，并返回被删除的结点
            BSTNode<T>* remove(BSTNode<T>* &tree, BSTNode<T> *z);

            // 销毁二叉树
            void destroy(BSTNode<T>* &tree);

            // 打印二叉树
            void print(BSTNode<T>* tree, T key, int direction);
    };

BSTree是二叉树。它包含二叉查找树的根节点和二叉查找树的操作。二叉查找树的操作中有许多重载函数，例如insert()函数，其中一个是内部接口，另一个是提供给外部的接口。

 

## 2 遍历

这里讲解前序遍历、中序遍历、后序遍历3种方式。

### 2.1 前序遍历

若二叉树非空，则执行以下操作：  
(01) 访问根结点；  
(02) 先序遍历左子树；  
(03) 先序遍历右子树。

前序遍历代码

    template <class T>
    void BSTree<T>::preOrder(BSTNode<T>* tree) const
    {
        if(tree != NULL)
        {
            cout<< tree->key << " " ;
            preOrder(tree->left);
            preOrder(tree->right);
        }
    }

    template <class T>
    void BSTree<T>::preOrder() 
    {
        preOrder(mRoot);
    }

 

### 2.2 中序遍历

若二叉树非空，则执行以下操作：  
(01) 中序遍历左子树；  
(02) 访问根结点；  
(03) 中序遍历右子树。

中序遍历代码

    template <class T>
    void BSTree<T>::inOrder(BSTNode<T>* tree) const
    {
        if(tree != NULL)
        {
            inOrder(tree->left);
            cout<< tree->key << " " ;
            inOrder(tree->right);
        }
    }

    template <class T>
    void BSTree<T>::inOrder() 
    {
        inOrder(mRoot);
    }

 

### 2.3 后序遍历

若二叉树非空，则执行以下操作：  
(01) 后序遍历左子树；  
(02) 后序遍历右子树；  
(03) 访问根结点。

后序遍历代码

    template <class T>
    void BSTree<T>::postOrder(BSTNode<T>* tree) const
    {
        if(tree != NULL)
        {
            postOrder(tree->left);
            postOrder(tree->right);
            cout<< tree->key << " " ;
        }
    }

    template <class T>
    void BSTree<T>::postOrder() 
    {
        postOrder(mRoot);
    }

 

 

看看下面这颗树的各种遍历方式：

![img](/media/pic/datastruct_algrithm/tree/bstree/bstree_06.jpg)

对于上面的二叉树而言，  
(01) 前序遍历结果： **3 1 2 5 4 6**  
(02) 中序遍历结果： **1 2 3 4 5 6**  
(03) 后序遍历结果： **2 1 4 6 5 3**

 

## 3. 查找

递归版本的代码

    template <class T>
    BSTNode<T>* BSTree<T>::search(BSTNode<T>* x, T key) const
    {
        if (x==NULL || x->key==key)
            return x;

        if (key < x->key)
            return search(x->left, key);
        else
            return search(x->right, key);
    }

    template <class T>
    BSTNode<T>* BSTree<T>::search(T key) 
    {
        search(mRoot, key);
    }

非递归版本的代码

    template <class T>
    BSTNode<T>* BSTree<T>::iterativeSearch(BSTNode<T>* x, T key) const
    {
        while ((x!=NULL) && (x->key!=key))
        {
            if (key < x->key)
                x = x->left;
            else
                x = x->right;
        }

        return x;
    }

    template <class T>
    BSTNode<T>* BSTree<T>::iterativeSearch(T key)
    {
        iterativeSearch(mRoot, key);
    }


## 4. 最大值和最小值

查找最大值的代码

    template <class T>
    BSTNode<T>* BSTree<T>::maximum(BSTNode<T>* tree)
    {
        if (tree == NULL)
            return NULL;

        while(tree->right != NULL)
            tree = tree->right;
        return tree;
    }

    template <class T>
    T BSTree<T>::maximum()
    {
        BSTNode<T> *p = maximum(mRoot);
        if (p != NULL)
            return p->key;

        return (T)NULL;
    }

查找最小值的代码

    template <class T>
    BSTNode<T>* BSTree<T>::minimum(BSTNode<T>* tree)
    {
        if (tree == NULL)
            return NULL;

        while(tree->left != NULL)
            tree = tree->left;
        return tree;
    }

    template <class T>
    T BSTree<T>::minimum()
    {
        BSTNode<T> *p = minimum(mRoot);
        if (p != NULL)
            return p->key;

        return (T)NULL;
    }

 

## 5. 前驱和后继

节点的前驱：是该节点的左子树中的最大节点。  
节点的后继：是该节点的右子树中的最小节点。

查找前驱节点的代码

    /* 
     * 找结点(x)的前驱结点。即，查找"二叉树中数据值小于该结点"的"最大结点"。
     */
    template <class T>
    BSTNode<T>* BSTree<T>::predecessor(BSTNode<T> *x)
    {
        // 如果x存在左孩子，则"x的前驱结点"为 "以其左孩子为根的子树的最大结点"。
        if (x->left != NULL)
            return maximum(x->left);

        // 如果x没有左孩子。则x有以下两种可能：
        // (01) x是"一个右孩子"，则"x的前驱结点"为 "它的父结点"。
        // (01) x是"一个左孩子"，则查找"x的最低的父结点，并且该父结点要具有右孩子"，找到的这个"最低的父结点"就是"x的前驱结点"。
        BSTNode<T>* y = x->parent;
        while ((y!=NULL) && (x==y->left))
        {
            x = y;
            y = y->parent;
        }

        return y;
    }

查找后继节点的代码

    /* 
     * 找结点(x)的后继结点。即，查找"二叉树中数据值大于该结点"的"最小结点"。
     */
    template <class T>
    BSTNode<T>* BSTree<T>::successor(BSTNode<T> *x)
    {
        // 如果x存在右孩子，则"x的后继结点"为 "以其右孩子为根的子树的最小结点"。
        if (x->right != NULL)
            return minimum(x->right);

        // 如果x没有右孩子。则x有以下两种可能：
        // (01) x是"一个左孩子"，则"x的后继结点"为 "它的父结点"。
        // (02) x是"一个右孩子"，则查找"x的最低的父结点，并且该父结点要具有左孩子"，找到的这个"最低的父结点"就是"x的后继结点"。
        BSTNode<T>* y = x->parent;
        while ((y!=NULL) && (x==y->right))
        {
            x = y;
            y = y->parent;
        }

        return y;
    }


## 6. 插入

插入节点的代码

    /* 
     * 将结点插入到二叉树中
     *
     * 参数说明：
     *     tree 二叉树的根结点
     *     z 插入的结点
     */
    template <class T>
    void BSTree<T>::insert(BSTNode<T>* &tree, BSTNode<T>* z)
    {
        BSTNode<T> *y = NULL;
        BSTNode<T> *x = tree;

        // 查找z的插入位置
        while (x != NULL)
        {
            y = x;
            if (z->key < x->key)
                x = x->left;
            else
                x = x->right;
        }

        z->parent = y;
        if (y==NULL)
            tree = z;
        else if (z->key < y->key)
            y->left = z;
        else
            y->right = z;
    }

    /* 
     * 将结点(key为节点键值)插入到二叉树中
     *
     * 参数说明：
     *     tree 二叉树的根结点
     *     key 插入结点的键值
     */
    template <class T>
    void BSTree<T>::insert(T key)
    {
        BSTNode<T> *z=NULL;

        // 如果新建结点失败，则返回。
        if ((z=new BSTNode<T>(key,NULL,NULL,NULL)) == NULL)
            return ;

        insert(mRoot, z);
    }

注：本文实现的二叉查找树是允许插入相同键值的节点的。若想禁止二叉查找树中插入相同键值的节点，可以参考"二叉查找树(一)之 图文解析 和 C语言的实现"中的插入函数进行修改。

 

## 7. 删除

删除节点的代码

    /* 
     * 删除结点(z)，并返回被删除的结点
     *
     * 参数说明：
     *     tree 二叉树的根结点
     *     z 删除的结点
     */
    template <class T>
    BSTNode<T>* BSTree<T>::remove(BSTNode<T>* &tree, BSTNode<T> *z)
    {
        BSTNode<T> *x=NULL;
        BSTNode<T> *y=NULL;

        if ((z->left == NULL) || (z->right == NULL) )
            y = z;
        else
            y = successor(z);

        if (y->left != NULL)
            x = y->left;
        else
            x = y->right;

        if (x != NULL)
            x->parent = y->parent;

        if (y->parent == NULL)
            tree = x;
        else if (y == y->parent->left)
            y->parent->left = x;
        else
            y->parent->right = x;

        if (y != z) 
            z->key = y->key;

        return y;
    }

    /* 
     * 删除结点(z)，并返回被删除的结点
     *
     * 参数说明：
     *     tree 二叉树的根结点
     *     z 删除的结点
     */
    template <class T>
    void BSTree<T>::remove(T key)
    {
        BSTNode<T> *z, *node; 

        if ((z = search(mRoot, key)) != NULL)
            if ( (node = remove(mRoot, z)) != NULL)
                delete node;
    }

 

## 8. 打印

打印二叉查找树的代码

    /*
     * 打印"二叉查找树"
     *
     * key        -- 节点的键值 
     * direction  --  0，表示该节点是根节点;
     *               -1，表示该节点是它的父结点的左孩子;
     *                1，表示该节点是它的父结点的右孩子。
     */
    template <class T>
    void BSTree<T>::print(BSTNode<T>* tree, T key, int direction)
    {
        if(tree != NULL)
        {
            if(direction==0)    // tree是根节点
                cout << setw(2) << tree->key << " is root" << endl;
            else                // tree是分支节点
                cout << setw(2) << tree->key << " is " << setw(2) << key << "'s "  << setw(12) << (direction==1?"right child" : "left child") << endl;

            print(tree->left, tree->key, -1);
            print(tree->right,tree->key,  1);
        }
    }

    template <class T>
    void BSTree<T>::print()
    {
        if (mRoot != NULL)
            print(mRoot, mRoot->key, 0);
    }

 

## 9. 销毁

销毁二叉查找树的代码

    /*
     * 销毁二叉树
     */
    template <class T>
    void BSTree<T>::destroy(BSTNode<T>* &tree)
    {
        if (tree==NULL)
            return ;

        if (tree->left != NULL)
            return destroy(tree->left);
        if (tree->right != NULL)
            return destroy(tree->right);

        delete tree;
        tree=NULL;
    }

    template <class T>
    void BSTree<T>::destroy()
    {
        destroy(mRoot);
    }

 
<a name="anchor3"></a>
# 第3部分 二叉查找树的C++实现(完整源码)

点击查看：[源代码][link_source_code]

关于二叉查找树的C++实现有两点需要补充说明的:  
**第1点：采用了STL模板。因此，二叉查找树支持任意数据类型。**  
**第2点：将二叉查找树的"声明"和"实现"都位于BSTree.h中。这是因为，在二叉查找树的实现采用了模板；而C++编译器不支持对模板的分离式编译！**

 
上面的BSTreeTest.cpp是二叉查找树树的测试程序，运行结果如下：

    == 依次添加: 1 5 4 3 2 6 
    == 前序遍历: 1 5 4 3 2 6 
    == 中序遍历: 1 2 3 4 5 6 
    == 后序遍历: 2 3 4 6 5 1 
    == 最小值: 1
    == 最大值: 6
    == 树的详细信息: 
     1 is root
     5 is  1's  right child
     4 is  5's   left child
     3 is  4's   left child
     2 is  3's   left child
     6 is  5's  right child

    == 删除根节点: 3
    == 中序遍历: 1 2 4 5 6 

 

下面对测试程序的流程进行分析！

(01) 新建"二叉查找树"root。

(02) 向二叉查找树中依次插入1,5,4,3,2,6 。如下图所示：

![img](/media/pic/datastruct_algrithm/tree/bstree/bstree_07.jpg)
 

(03) 打印树的信息

插入1,5,4,3,2,6之后，得到的二叉查找树如下：

![img](/media/pic/datastruct_algrithm/tree/bstree/bstree_08.jpg)

前序遍历结果: 1 5 4 3 2 6  
中序遍历结果: 1 2 3 4 5 6  
后序遍历结果: 2 3 4 6 5 1  
最小值是1，而最大值是6。

 

(04) 删除节点3。如下图所示：

![img](/media/pic/datastruct_algrithm/tree/bstree/bstree_09.jpg)
 

(05) 重新遍历该二叉查找树。

中序遍历结果: 1 2 4 5 6

 
[link_source_code]: https://github.com/wangkuiwu/datastructs_and_algorithm/tree/master/source/tree/bstree/cplus

