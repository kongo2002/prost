/*
 * Copyright 2013 Gregor Uhlenheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kongo2002.android.prost

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

/* vim: set et sw=2 sts=2: */
