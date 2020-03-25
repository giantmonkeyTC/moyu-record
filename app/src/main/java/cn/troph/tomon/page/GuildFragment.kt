package cn.troph.tomon.page

import android.app.Activity
import android.gesture.Gesture
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment

import cn.troph.tomon.R

class GuildFragment : Fragment(),GestureDetector.OnGestureListener{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view :View = inflater.inflate(R.layout.fragment_guild, container, false)
        view.setOnTouchListener(View.OnTouchListener(
            function = fun(v:View, event: MotionEvent):Boolean{
                if(event.action == MotionEvent.ACTION_DOWN){
                    Toast.makeText(context,"action down",Toast.LENGTH_SHORT).show()
                }
               return true
            }
        ))
        // Inflate the layout for this fragment
        return view
    }

    override fun onShowPress(e: MotionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDown(e: MotionEvent?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFling(
        downEvent: MotionEvent?,
        moveEvent: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        var result : Boolean = false
        var diffY : Float = moveEvent!!.getY() - downEvent!!.getY()
        var diffX : Float = moveEvent!!.getX() - downEvent!!.getX()
        if (Math.abs(diffX)>Math.abs(diffY)){
            //left or right
            val SWIPE_THRESHOLD : Int = 100
            val VELOCITY_THRESHOLD: Int = 100
            if(Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX)> VELOCITY_THRESHOLD){
                if (diffX>0)
                    onSwipeRight()
                else onSwipeLeft()
                result = true
            }
        }
        else{
            //up or down
        }
       return result
    }

    private fun onSwipeLeft() {
       Toast.makeText(context,"swipe left",Toast.LENGTH_LONG).show()
    }

    private fun onSwipeRight() {
        Toast.makeText(context,"swipe right ",Toast.LENGTH_LONG).show()
    }



    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLongPress(e: MotionEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
