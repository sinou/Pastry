import akka.actor._
import akka.pattern._
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor._
import scala.util.Random

import scala.swing._
import java.awt.event.ComponentListener
import java.awt.event.ComponentEvent

import scala.collection.mutable.ListBuffer

sealed trait Pastry
case object Initiate extends Pastry
class Message(Key: Int, mess: String, Sender: Int){
	val key: Int = Key
	val message: String = mess
	var hop: Int = 0;
	val sender: Int = Sender;
}
class StateMessage(r:Array[Int],l:List[Int],m:Array[Int],nId:Int,gridX:Int,gridY:Int)
{
  var R:Array[Int] = r
  var L:List[Int] = l
  var M:Array[Int] = m
  val NodeId:Int=nId;
  val GridX:Int = gridX;
  val GridY:Int = gridY;
  
  
 }

object Project3{
  
  def main(args: Array[String]){
    
    if(args.length < 2){
      
      println("enter valid inputs")
      
    }else{
    
    val system = ActorSystem("Project3System")
    val networkParam = new netInfo(4,4,4,8,100);
    
    val supervisor = system.actorOf(Props(new Supervisor(networkParam)), "Supervisor")
    
    supervisor ! Initiate
    
    }
    
  }

}

class Supervisor(net:netInfo) extends Actor{
  
  var nodes:List[ActorRef] = List. empty
  
  var nodeIds: List[Int] = List.empty
  var gridPoints:List[(Int,Int)] = List.empty
   
  override def preStart = 
   {
    
    for(i<- 0 until 12)
    {
    makeANode()
    }
    
   }
  def makeANode()
{
var  rndX:Int = 0
    var rndY:Int = 0 
    var rndNodeId :Int = 0
    do
   {
     rndX = (math.random * net.root).toInt
     rndY = (math.random * net.root).toInt
   }
   while(gridPoints.contains((rndX,rndY)))
     gridPoints =   gridPoints ::: List((rndX,rndY))
     do
   {
     rndNodeId = (math.random * net.NumNode).toInt
   }
   while(nodeIds.contains( rndNodeId))
      nodeIds = nodeIds ::: List(rndNodeId)
     var neighBour:ActorRef = null
     var neighBourIndx: Int = findNeigh(rndX,rndY)
     println("NeighbourIdx :"+neighBourIndx);
      if(neighBourIndx != -1)
      {
        neighBour = nodes(neighBourIndx)
      }
 nodes =   nodes::: List(context.system.actorOf(Props(new Node(rndX,rndY,rndNodeId,net,neighBour)),rndNodeId+"") )
 updateNet()
 println( (nodeIds))
   println(gridPoints)
   
} 
  def updateNet()
  {
    net.nodeIds = this.nodeIds
    net.gridPoints= this.gridPoints
  }
  def receive = {
    
    case Initiate => //println("Initiating");
  
   
  }
  
 def findNeigh(x: Int, y: Int): Int ={
     var xs: Int = x;
     var ys: Int = y;
     var d, i: Int = 0;
     for(d <- 1 to (net.root + net.root/1.2).toInt){
       for(i <- 0 to (d)){
         var x1 = xs - d + i;
         var y1 = ys - i;
       		//println(x1, y1)
       		if(gridPoints.contains((x1, y1))){
       		  //println(gridPoints.indexOf((x1, y1)))      		  
       		  //println(x1, y1)
       		  return gridPoints.indexOf((x1, y1))
       		}
       		  
        // Check point (x1, y1)

       		var x2 = xs + d - i;
       		var y2 = ys + i;
       		//println(x2, y2)
       		if(gridPoints.contains((x2, y2))){
       		  //println(gridPoints.indexOf((x2, y2)))
       		  //println(x2, y2)
       		  return gridPoints.indexOf((x2, y2))
       		}
        // Check point (x2, y2)
       }
       for(i <- 1 to (d - 1)){
         
    	   var x1 = xs - i;
    	   var y1 = ys + d - i;
       		//println(x1, y1)
       		if(gridPoints.contains((x1, y1))){
       		  //println(gridPoints.indexOf((x1, y1)))
       		  //println(x1, y1)
       		  return gridPoints.indexOf((x1, y1))
       		}
        // Check point (x1, y1)

    	    var x2 = xs + d - i;
       		var y2 = ys - i;
       		//println(x2, y2)
       		if(gridPoints.contains((x2, y2))){
       		 // println(gridPoints.indexOf((x2, y2)))
       		  //println(x2, y2)
       		  return gridPoints.indexOf((x2, y2))
       		}
        // Check point (x2, y2)
         
       }
     }
     -1
   }
  
}


   class netInfo(l:Int,m:Int,b:Int,d:Int,num:Int)   {
   val L = l
   val M = m
   val digits = d
   val NumNode = num
   val base = b
   val root =  Math.ceil(Math.sqrt(num.toDouble)).toInt
   var nodeIds: List[Int] = List.empty
   var gridPoints:List[(Int,Int)] = List.empty
   }
 class Node(Gridx:Int,Gridy:Int,nodeId:Int,net:netInfo,initialNeigh:ActorRef) extends Actor
 {
  var R:Array[Int] = Array.empty
  var L:List[Int] = List.empty
  var M:Array[Int] = Array.empty
  
  var NodeId = nodeId
   override def preStart () =
   {
   R = new Array[Int]((net.base)*net.digits) 
   L = List[Int](NodeId) 
   M = new Array[Int](net.M)
   }
  
   def receive =
   {
     case a:Message =>
       a.message match
       {
         case "join"=>
         			
         			  sendMessage(a.key,new StateMessage(R,L,M,this.nodeId,this.Gridx,this.Gridy))
         			  if(checkInLeaf(a.key))
         			  {
         			    consumeMessage(a);
         			  }
         			  else
         			  {
         			    passMessage(a)
         			  }
         case "reg"=>
           			if(checkInLeaf(a.key))
         			  {
         			    consumeMessage(a);
         			  }
         			  else
         			  {
         			    passMessage(a)
         			  }         
       }
     case a:StateMessage =>
       updateState(a);
       
   }
   def consumeMessage(a:Message)
   {
     println("Message Source: "+ a.sender + "Destination: " +a.key+" Ended at NodeId: "+ this.nodeId+" Message Type"+ a.message+"Hops: "+ a.hop)
   }
   def passMessage(a:Message)
   {
     a.hop= a.hop + 1;
    var nextNode:Int =  pickANode(a.key)
     if(nextNode== -1)
     {
       consumeMessage(a)
     }
     else
     {
       sendMessage(nextNode, a)
     }
     
     
   }
   def pickANode(destination:Int):Int=
   {
     var temp:Int =  R( locationInRTable(destination));
    var ret:Int = -1
     if(temp!= -1 && temp!= -2)
    {
     ret =  temp
    }
    else
    {
      var row:Int = getRRowIndx(destination)
      var min:Int = Int.MaxValue
      for(i<- 0 until net.base)
      {
    	  if(R(row + i)!= -1 && R(row + i)!= -2)
    	  {
    		  if(getDistance(R(row + i))<min)
    		  {
    			  min = getDistance(R(row + i))
    					  ret = i
    		  }
        
    	  }
      }
    }
     ret
   }
   def locationInRTable(num:Int):Int=
   {
     var temp:Int = lengthOfPrefix(nodeId, num)
     
     (temp ) * net.base + numberInNetBase(num).substring(temp,temp + 1).toInt
     
   }
   def getRRowIndx(num:Int):Int = {
     (lengthOfPrefix(this.nodeId, num)) *  net.base
   }
   def getDistance(num:Int):Int=
   {
     var tuple = net.gridPoints(net.nodeIds.indexOf(num))
     distanceFunction(this.Gridx, this.Gridy, tuple._1, tuple._1)
     
   }
   def updateState(a:StateMessage){
     val giverR = a.R
     val giverL = a.L
     val giverM = a.M
     val giverNodeId = a.NodeId
     var i, j: Int = 0
     var currentPrefix,temp: Int = 0
      if (R(locationInRTable(giverNodeId)) == -1 | R(locationInRTable(giverNodeId)) == -2) {
        R(locationInRTable(giverNodeId)) = giverNodeId
       }
     currentPrefix = lengthOfPrefix(nodeId, giverNodeId)
     copyRow(R, giverR, (temp + 1) * net.base, (currentPrefix + 1) * net.base)
     
     if(Math.abs(nodeId - giverNodeId) <= net.L/2){
      L = L ::: List(giverNodeId)
     }
     
     if(getDistance(giverNodeId) <= 5){ // change the number later
       M(M.length) = giverNodeId
     }
     
   }
   
   def copyRow(seekerR: Array[Int], giverR: Array[Int], start: Int, end: Int){
     var i: Int = 0
     for(i <- start until end){
       if(giverR(i) < 0 & seekerR(i) < 0){
         seekerR(i) = -2
       }else if(giverR(i) >= 0){
         if(seekerR(locationInRTable(giverR(i))) < 0){
       	seekerR(locationInRTable(giverR(i))) = giverR(i)
         }else{
           if(getDistance(giverR(i)) < getDistance(seekerR(locationInRTable(giverR(i)))))
           seekerR(locationInRTable(giverR(i))) = getDistance(giverR(i))
         }
       }
     }
   }
   def distanceFunction(x1:Int,y1:Int,x2:Int,y2:Int):Int = 
   {
     math.abs(x1 - x2) + math.abs(y1 - y2)
   }
   def checkInLeaf(key:Int):Boolean = L.contains(key)
   def readR(row:Int,col:Int):Int= ((row * net.base)+ col)
   def writeR(row:Int,col:Int,value:Int) 
   {
     R((row * net.base)+ col)  = value
    
   }
  def numberInNetBase(num:Int):String=
   {
     var aBase = Integer.toString(num, net.base);
     aBase = ("0" * (net.digits - aBase.length() )) + aBase;
     aBase
   }
   def lengthOfPrefix(a:Int,b:Int):Int = 
   {
//     var aBase = Integer.toString(a, net.base);
//     var bBase = Integer.toString(b, net.base);
//     aBase = ("0" * (net.digits - aBase.length() )) + aBase
//     bBase = ("0" * (net.digits - bBase.length() )) + bBase
     pref( numberInNetBase(a), numberInNetBase(b)).length
   }
   def pref(s: String, t: String, out: String = ""): String = {
 
  if (s == "" || t == "" || s(0) != t(0)) out
  else pref(s.substring(1), t.substring(1), out + s(0))
}
    def sendMessage(nodeNum:Int, Message:Any)
  {
   findActor(nodeNum.toString) ! Message
  }
  def findActor(nodeName:String):ActorSelection =   context.actorSelection("../"+nodeName)
 }





