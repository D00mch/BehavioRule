# BehavioRule
The way to set up a CollapsingToolbarLayout in the CoordinatorLayout with a custom Behavior

You can read about it on [habr.com](https://habr.com/post/426369/) (in Russian).

![example](https://github.com/Liverm0r/BehavioRule/blob/master/collapsing_toolbar_example.gif)

## Why

When you work with CollaplingToolbar, you are writing behaviors on different views. Some views are located inside CollapsingToolbarLayout and are managed by OnOffsetChangedListener, some views are out. When you need to update this logic, you will have to check several classes to grasp all the picture and it may easily become a spaghetti-code. 

I propose a solution where you have a _**declarative way**_ to describe the collapsing logic _**in one class**_.

## Dependencies

Add to your app build.gradle

```groovy
implementation 'com.github.Liverm0r:BehavioRule:1.0.1'
```

You also have to add this in your project build.gradle

```groovy
allprojects {
    repositories {
        //...
        maven { url 'https://jitpack.io' }
    }
}
```
[![Build Status](https://travis-ci.org/sockeqwe/AdapterDelegates.svg?branch=master)](https://jitpack.io/#Liverm0r/BehavioRule)

## Quick usage guide

With this solution you need to implement [rules](https://github.com/Liverm0r/BehavioRule/blob/master/library/src/main/java/com/behaviorule/arturdumchev/library/Rules.kt) for each view you want to control. 

For example, if you are to change the alpha channel of your view depending on the Appbar's scroll offset, you will write:
```kotlin
BRuleAlpha(min = 0f, max = 1f)
```
This makes the view invisible when toolbar is collapsed, and completely visible when stretched. But what if you want your view to be visible on collapse and change alpha to the value of 0.5 when completely stretched? And - lets say - you want an accelerating changes. Easy task:
```kotlin
BRuleAlpha(
        min = 0f, max = 0.5f,
        interpolator = ReverseInterpolator(AccelerateInterpolator())
)
```

You can define any amount of rules for your **view**. To declare a view with rules, you write:
```kotlin
RuledView(
        view = someView,
        rules = listOf(alphaRule, yRule, scaleRule)
)
```

Finally, you need all of your rules to be declared in one place and work like charm. [BehaviorByRules](https://github.com/Liverm0r/BehavioRule/blob/master/library/src/main/java/com/behaviorule/arturdumchev/library/BehaviorByRules.kt) is a class you want to implement. It is just an abstract class inherited from CoordinatorLayout.Behavior.

Implementing this class, you need to override 4 functions:

1. ```provideCollapsingToolbar()``` - a link to the CollapsingToolbarLayout of the current CoordinatorLayout you are working with.
2. ```provideAppbar()``` - a link to the AppbarLayout that contains the CollapsingToolbarLayout.
3. ```alcAppbarHeight()``` — basically the height of your custom toolbar, when stretched.
4. ```setUpViews()``` - a place where you add all of your rules.

That's all. Check the example: [TopInfoBehavior](https://github.com/Liverm0r/BehavioRule/blob/master/app/src/main/java/com/behaviorule/arturdumchev/behaviorule/TopViewBehavior.kt).

## Layout structure

All tags that not require your attention are removed:
```xml
<CoordinatorLayout>

    <AppBarLayout
        android:layout_height="wrap_content">

        <CollapsingToolbarLayout
            app:layout_scrollFlags="scroll|exitUntilCollapsed">

            <Toolbar
                android:layout_height="@dimen/toolbar_height"
                app:layout_collapseMode="pin"/>

        </CollapsingToolbarLayout>

    </AppBarLayout>

    <!--Our toolbar is here-->
    <!--It is possible to include any layout instead of this RelativeLayout-->
    <RelativeLayout 
        android:translationZ="5dp"
        app:layout_behavior="TopInfoBehavior"/>

    <NestedScrollView
        app:layout_behavior="@string/appbar_scrolling_view_behavior">
    </NestedScrollView>

    <FloatingActionButton
        app:layout_anchor="@id/nesteScroll"
        app:layout_anchorGravity="right"/>

</CoordinatorLayout>
```

## Existing BRules:

  * ```BRuleScale``` – increase scale by ratio.
  * ```BRuleAlpha``` - view's alpha channel.
  * ```BRuleYOffset``` - view's y.
  * ```BRuleXOffset``` - view's x.
  * ```BRuleAppear``` - make view disappear at some offset (work with alpha channel).
  * ```ThresholdRule``` - wrapper class to force a view to work in some range.

## Existing interpolators:

  * ```ReverseInterpolator``` - wrapper class that takes any interpolator and reverse it's value.
  * ```TurnBackInterpolator``` - wrapper class, that can reverse interpolation at some scroll offset.

## Some difficult example
Imagine you want your view to change the alpha from 0.6 (when collapsed) till 1.0. You also want it to become invisible on first 10% of scroll. And you want it to increase it's size two times when half-collapsed and then turn on to the normal scale. The described behavior can be declaring with two rules:
```kotlin
val appearedUntil = 0.1f // this is 10% of scroll, as we have values from 0.0 till 1.0

// using second constructor of RuledVeiw to save some lines :)
RuledVeiw(
    someTrickyView,
    BRuleAppear(visibleUntil = appearedUntil),
    BRuleAlpha(min = 0.6f, max = 1f).workInRange(from = appearedUntil, to = 1f),
    BRuleScale(
            min = 1f, max = 2f,
            interpolator = TurnBackInterpolator(
                    interpolator = LinearInterpolator(),
                    turnRatio = 0.5f
            )
    )
)
```
```workInRange``` is just a function that wraps your rule with ThresholdRule.

  ## License

```
Copyright 2017 Artur Dumchev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
