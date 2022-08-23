import java.util.Queue;

public class case1 {

    void Marks(Queue<Integer> marksQueue) {
        int Pass, Fail, Count;
        Pass = 0 ;
        Fail = 0 ;
        Count = 0 ;
        while (!marksQueue.isEmpty()) {
            int Marks = marksQueue.poll();
            if (Marks >= 40)
                Pass = Pass + 1;
            if (Marks < 40)
                Fail = Fail + 1;
            Count = Count + 1;
        }
        System.out.println(Pass);
        System.out.println(Fail);
        System.out.println(Count);
    }

}
