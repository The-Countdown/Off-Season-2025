package org.firstinspires.ftc.masters.components;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class Outtake implements Component{

    private PIDController controller;
    private final FtcDashboard dashboard = FtcDashboard.getInstance();

    public static double p = 0.0045, i = 0.0, d = 0.00001;
    public static double f = 0.09;

    public int target = 0;

    private final Servo claw;
    public final Servo wrist, angleLeft, angleRight, position;
    private final DcMotor outtakeSlideLeft;
    private final DcMotor outtakeSlideRight;
    public final DcMotor outtakeSlideEncoder;

    private Status status;

    private ElapsedTime elapsedTime = null;


    Telemetry telemetry;
    Init init;

    enum Mode {
        Sample,
        Specimen
    }

    public enum Status {
        Transfer(0),
        ScoreSpecimen(0),
        Wall (0),
        Bucket(0),
        InitWall(0),
        InitAutoSpec(0),
        InitAutoSample(0),

        TransferToBucket_CloseClaw(100),
        TransferToBucket_Lift(0),
        TransferToBucket_Move(500),

        TransferToWall_Up(100),
        TransferToWall_Back(500),
        TransferToWall_Final(100),

        BucketToTransfer_Down(400),
        BucketToTransfer_Open(100),
        BucketToTransfer_Final(100),


        WallToFront_lift(300),
        WallToFront_move(350),
        WallToFront3 (200),

        SpecimenToWall_MoveBack(1200),
        SpecimenToWall_Final(100),

        Front(300);

        private final long time;

        private Status (long time){
            this.time= time;
        }

        public long getTime() {
            return time;
        }
    }

    public Outtake(Init init, Telemetry telemetry){

        this.init=init;
        this.telemetry=telemetry;

        claw= init.getClaw();
        wrist= init.getWrist();


        this.outtakeSlideLeft =init.getOuttakeSlideLeft();
        this.outtakeSlideRight =init.getOuttakeSlideRight();
        this.outtakeSlideEncoder =init.getRightFrontMotor();
        angleLeft = init.getAngleLeft();
        angleRight = init.getAngleRight();
        position = init.getPosition();
        initializeHardware();
        status = Status.Front;

    }

    public void initializeHardware() {

        controller = new PIDController(p, i, d);
        controller.setPID(p, i, d);

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        target = 0;

    }

    public void initTeleopWall(){
        position.setPosition(ITDCons.positionBack);
        angleLeft.setPosition(ITDCons.angleBack);
        angleRight.setPosition(ITDCons.angleBack);
        claw.setPosition(ITDCons.clawOpen);
        status= Status.InitWall;
        controller.setP(0.002);
        position.getController().pwmDisable();

    }

    public void initAutoSpecimen(){
        //position.setPosition(ITDCons.positionInitSpec);
        setAngleServoScore();
        closeClaw();
        wrist.setPosition(ITDCons.wristFront);
        status= Status.InitAutoSpec;
    }

    public void initAutoSample(){
        position.setPosition(ITDCons.positionBack);
        angleLeft.setPosition(ITDCons.angleBack);
        angleRight.setPosition(ITDCons.angleBack);
        closeClaw();
        status= Status.InitAutoSample;

    }

    public void setPID(double p){
        controller.setP(p);
    }

    public void openClaw() {
        claw.setPosition(ITDCons.clawOpen);
    }

    public void closeClaw() {
        claw.setPosition(ITDCons.clawClose);
    }

    public void moveToPickUpFromWall(){
        if (status==Status.ScoreSpecimen) {
            target = ITDCons.intermediateTarget;
            wrist.setPosition(ITDCons.wristBack);
            openClaw();

            position.setPosition(ITDCons.positionBack);
            setAngleServoToMiddle();
            status= Status.SpecimenToWall_MoveBack;
            elapsedTime = new ElapsedTime();
        }
        if (status == Status.InitWall){
            target = ITDCons.WallTarget;
            openClaw();

            status= Status.Wall;
        }

    }

    public void moveToTransfer(){

        target = ITDCons.intermediateTarget;
//        wrist.setPosition(ITDCons.wristFront);
        position.setPosition(ITDCons.positionTransfer);
        setAngleServoScore();
        status = Status.BucketToTransfer_Down;

    }

    public void moveToBucket(){
        closeClaw();
        status = Status.TransferToBucket_CloseClaw;
//        wrist.setPosition(ITDCons.wristBack);
//        position.setPosition(ITDCons.positionBack);
//        angleRight.setPosition(ITDCons.angleBack);
//        angleLeft.setPosition(ITDCons.angleBack);
    }

    public void scoreSpecimen(){
        if (status==Status.InitAutoSpec){
            status= Status.WallToFront_move;
        } else {
            status = Status.WallToFront_lift;
        }

        target = ITDCons.SpecimenTarget;

        elapsedTime = new ElapsedTime();
    }

    public void releaseSpecimen(){
        target= ITDCons.ReleaseTarget;
    }

    protected void moveSlide() {

//frontRight
        int rotatePos = -outtakeSlideEncoder.getCurrentPosition();

        telemetry.addData("rotatePos",rotatePos);
        double pid = controller.calculate(rotatePos, target);
        double lift=pid;
       // double lift = pid + f;

        telemetry.addData("lift", lift);

        outtakeSlideLeft.setPower(lift);
        outtakeSlideRight.setPower(lift);

    }

    public int getTarget(){
        return target;
    }

    public void setTarget(int target){
        this.target = target;
    }

    public int getExtensionPos(){
        return outtakeSlideEncoder.getCurrentPosition();
    }


    public void update(){
        moveSlide();

        telemetry.addData("status", status);

        switch (status){
            case WallToFront_lift:
                if (elapsedTime!=null && elapsedTime.milliseconds()> status.getTime()){
                    //
                   setAngleServoToMiddle();
                    elapsedTime = new ElapsedTime();
                    status = Status.WallToFront_move;
                }
                break;
            case WallToFront_move:
                if (elapsedTime!=null && elapsedTime.milliseconds()> status.getTime()){
                    position.setPosition(ITDCons.positionFront);

                    elapsedTime = new ElapsedTime();
                    status = Status.WallToFront3;
                }
                break;
            case WallToFront3:
                if (elapsedTime!=null && elapsedTime.milliseconds()>status.getTime()){
                    wrist.setPosition(ITDCons.wristFront);
                    setAngleServoScore();
                    elapsedTime = new ElapsedTime();
                    status = Status.Front;
                }
                break;
            case Front:
                if (elapsedTime!=null && elapsedTime.milliseconds()>status.getTime()){
                    elapsedTime=null;
                    status= Status.ScoreSpecimen;
                }
                break;
            case TransferToBucket_CloseClaw:
                if (elapsedTime!=null && elapsedTime.milliseconds()>status.getTime()){
                    target = ITDCons.BucketTarget;
                    elapsedTime= new ElapsedTime();
                    status = Status.TransferToBucket_Lift;
                }
                break;

            case TransferToBucket_Lift:

               //TODO: change from time to vertical slide position
                if (elapsedTime!=null && elapsedTime.milliseconds()>status.getTime()){
                    elapsedTime = new ElapsedTime();

                    setAngleServoToMiddle();
                    position.setPosition(ITDCons.positionBack);

                    status = Status.TransferToBucket_Move;
                }
                break;
            case TransferToBucket_Move:
                if (elapsedTime!=null && elapsedTime.milliseconds()>status.getTime()) {
                    elapsedTime = null;
                    status = Status.Bucket;
                }
                break;

            case BucketToTransfer_Down:

                if (elapsedTime!=null && elapsedTime.milliseconds()>status.getTime() && outtakeSlideEncoder.getCurrentPosition()<ITDCons.intermediateTarget+500){
                    elapsedTime = new ElapsedTime();
                    status = Status.BucketToTransfer_Open;
                    openClaw();
                    wrist.setPosition(ITDCons.wristFront);
                    target = ITDCons.transferPickupTarget;
                }
                break;
            case BucketToTransfer_Final:
                if (elapsedTime!=null && elapsedTime.milliseconds()>status.getTime()){
                    elapsedTime = new ElapsedTime();
                    status= Status.Transfer;
                    setAngleServoToFront();
                }
                break;
            case SpecimenToWall_MoveBack:
                if (elapsedTime!=null && elapsedTime.milliseconds()>status.getTime() && outtakeSlideEncoder.getCurrentPosition()<ITDCons.intermediateTarget+500) {
                    target = ITDCons.wallPickupTarget;
                    setAngleServoToBack();
                    status= Status.Wall;

                }
                break;


        }
    }

    private void setAngleServoToFront(){
        angleLeft.setPosition(ITDCons.angleFront);
        angleRight.setPosition(ITDCons.angleFront);
    }

    private void setAngleServoToMiddle(){
        angleLeft.setPosition(ITDCons.angleMiddle);
        angleRight.setPosition(ITDCons.angleMiddle);
    }

    private void setAngleServoToBack(){
        angleLeft.setPosition(ITDCons.angleBack);
        angleRight.setPosition(ITDCons.angleBack);
    }

    private void setAngleServoScore(){
        angleLeft.setPosition(ITDCons.angleScore);
        angleRight.setPosition(ITDCons.angleScore);
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
