package com.kongo2002.prost

import android.app.Activity
import android.view.View

object ImplicitHelpers {
  implicit def view2Typed(v : View) = new TypedViewHolder { def view = v }
  implicit def activity2Typed(a : Activity) = new TypedActivityHolder { def activity = a }
  
  implicit def function2OnClickListener(f : View => Unit) : View.OnClickListener = {
    new View.OnClickListener() {
      def onClick(v : View) {
        f(v);
      }
    }
  }
}