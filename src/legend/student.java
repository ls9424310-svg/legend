/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package legend;

/**
 *
 * @author SHARMA
 */
public class student {
    int serialNumber;
    String name;
    String roll_num;
    student(int s,String n,String r){
        this.serialNumber=s;
        this.name=n;
        this.roll_num=r;
    }
        public String  getRollNumber(){
            return roll_num;
            
        }
        public String  getName(){
            return name;
        }
        public int  getSerialNumber(){
            return serialNumber;
  
        }
        
    }
    

