package com.behaviorule.arturdumchev.behaviorule

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.behaviorule.arturdumchev.library.BRuleAlpha
import com.behaviorule.arturdumchev.library.BRuleAppear
import com.behaviorule.arturdumchev.library.BRuleScale
import com.behaviorule.arturdumchev.library.BRuleXOffset
import com.behaviorule.arturdumchev.library.BRuleYOffset
import com.behaviorule.arturdumchev.library.BehaviorRule
import com.behaviorule.arturdumchev.library.InitialViewDetails
import com.behaviorule.arturdumchev.library.RuledView
import com.behaviorule.arturdumchev.library.ReverseInterpolator
import com.behaviorule.arturdumchev.library.BehaviorByRules
import com.behaviorule.arturdumchev.library.pixels
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.top_view.view.*

/**
 * @author arturdumchev on 13/10/2018.
 */
class TopInfoBehavior(
        context: Context?,
        attrs: AttributeSet?
) : BehaviorByRules(context, attrs) {

    override fun calcAppbarHeight(child: View): Int = with(child) {
        return (height + pixels(R.dimen.toolbar_height)).toInt()
    }

    override fun View.provideAppbar(): AppBarLayout = ablAppbar
    override fun View.provideCollapsingToolbar(): CollapsingToolbarLayout = ctlToolbar
    override fun canUpdateHeight(progress: Float): Boolean = progress >= GONE_VIEW_THRESHOLD

    override fun View.setUpViews(): List<RuledView> = listOf(
            RuledView(
                    iTopDetails,
                    BRuleYOffset(
                            min = pixels(R.dimen.zero),
                            max = pixels(R.dimen.toolbar_height)
                    )
            ),
            RuledView(
                    tvTopDetails,
                    BRuleAlpha(min = 0.6f, max = 1f),
                    BRuleXOffset(
                            min = 0f, max = pixels(R.dimen.big_margin),
                            interpolator = ReverseInterpolator(AccelerateInterpolator())
                    ),
                    BRuleYOffset(
                            min = pixels(R.dimen.zero), max = pixels(R.dimen.dialog_padding),
                            interpolator = ReverseInterpolator(LinearInterpolator())
                    ),
                    BRuleAppear(0.1f),
                    BRuleScale(min = 0.8f, max = 1f),
                    object : BehaviorRule {
                        override fun manage(ratio: Float, details: InitialViewDetails, view: View) {
                            view.layoutParams = view.layoutParams
                        }
                    }
            ),
            RuledView(
                    tvPainIsTheArse,
                    BRuleAppear(appearedUntil = GONE_VIEW_THRESHOLD)
            ),
            RuledView(
                    tvCollapsedTop,
                    BRuleAppear(0.1f, true)
            ),
            RuledView(
                    tvTop,
                    BRuleAppear(appearedUntil = GONE_VIEW_THRESHOLD)
            ),
            imagesRuleFunc(ivTop, LinearInterpolator()),
            imagesRuleFunc(ivTop2, AccelerateInterpolator(0.7f)),
            imagesRuleFunc(ivTop3, AccelerateInterpolator())
    )

    private fun View.imagesRuleFunc(view: ImageView, interpolator: Interpolator) = RuledView(
            view,
            BRuleYOffset(
                    min = -(ivTop3.y - tvCollapsedTop.y),
                    max = 0f,
                    interpolator = DecelerateInterpolator(1.5f)
            ),
            BRuleXOffset(
                    min = 0f,
                    max = tvCollapsedTop.width.toFloat() + pixels(R.dimen.huge_margin),
                    interpolator = ReverseInterpolator(interpolator)
            )
    )


    companion object {
        const val GONE_VIEW_THRESHOLD = 0.8f
    }
}
