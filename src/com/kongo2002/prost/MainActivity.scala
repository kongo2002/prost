package com.kongo2002.prost;

import java.util.Date
import java.util.Calendar
import scala.collection.JavaConversions._
import scala.math.Ordering

import android.app.Activity
import android.view.View

import android.widget.Button
import android.widget.Toast

import android.os.Bundle

class MainActivity extends TypedActivity {
  
  lazy val newBeerBtn = findView(TR.newBeerBtn)
  lazy val totalTv = findView(TR.totalCountTv)
  lazy val perHour = findView(TR.perHourTv)
  lazy val drinksTv = findView(TR.totalDrinksTv)
  
  val order = Ordering.by[Beer, Date](x => x.bought)
  val drinks = new java.util.TreeSet[Beer](order)

  override def onCreate(state: Bundle) {
    super.onCreate(state)
    setContentView(R.layout.activity_example)
        
    newBeerBtn.setOnClickListener(new View.OnClickListener() {
      def onClick(v : View) {
        val beer = new Pint()
        
        drinks.add(beer)
    
        update
        
        Toast.makeText(getApplicationContext(), "Added new " + beer.name, Toast.LENGTH_SHORT).show()
      }
    })
    
    update
  }
  
  private def getLiters = {
    val liters = drinks.foldLeft(0.0)((a, d) => a + d.unitSize)
    liters / 1000.0
  }
  
  private def timeDiff(from: Date, to: Date) = {
    val calFrom = Calendar.getInstance()
    val calTo = Calendar.getInstance()
    
    calFrom.setTime(from)
    calTo.setTime(to)
    
    calTo.getTimeInMillis() - calFrom.getTimeInMillis()
  }
  
  private def getHourDiff = {
    val size = drinks.size()
    if (size > 0) {
      val now = new Date()
      val diff = timeDiff(drinks.first.bought, now)
      val hourDiff = diff / (1000.0 * 60)
      
      hourDiff
    }
    1.0
  }
  
  private def update {
    val count = drinks.size()
    val liters = getLiters
    val avg = liters / count
    
    totalTv.setText(liters.toString())
    perHour.setText((liters / getHourDiff).toString())
    drinksTv.setText(count.toString())
  }
}