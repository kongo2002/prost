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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup

import Implicits._
import DrinksDatabase.BarsCursor

class BarsFragment extends TypedFragment
  with Loggable {

  val OPTION_DELETE_BAR = 1

  override def onCreateView(inf: LayoutInflater, vg: ViewGroup, b: Bundle) = {
    logI("onCreateView")

    val view = inf.inflate(R.layout.bars_fragment, vg, false)

    setHasOptionsMenu(true)

    view
  }
}

/* vim: set et sw=2 sts=2: */
