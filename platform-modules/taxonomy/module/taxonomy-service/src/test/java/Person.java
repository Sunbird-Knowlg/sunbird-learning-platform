public class Person {

    private String name;

    public Person(String name) {
        this.name = name;
    }

    public String wakeUp(){
        return this.name + " wakes up...";
    }

    public String goToWork(boolean isWeekend){
        final String result;
        if (isWeekend){
            result = this.name + " does not go to work!";
        }
        else{
            result = this.name + " goes to work!";
        }

        return result;
    }
}