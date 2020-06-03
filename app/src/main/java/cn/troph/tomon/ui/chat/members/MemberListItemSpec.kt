package cn.troph.tomon.ui.chat.members

import android.graphics.Color
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge

@LayoutSpec
class MemberListItemSpec {

    companion object {
        @OnCreateLayout fun onCreateLayout(c: ComponentContext): Component {
            return Column.create(c)
                .paddingDip(YogaEdge.ALL, 16F)
                .backgroundColor(Color.WHITE)
                .child(
                    Text.create(c)
                        .text("Hello world")
                        .textSizeSp(40F)
                )
                .child(
                    Text.create(c)
                        .text("Litho tutorial")
                        .textSizeSp(20F)
                )
                .build()
        }

    }
}