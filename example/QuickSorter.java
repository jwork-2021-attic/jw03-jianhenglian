package example;
/**
 * @author ljh
 * @create 2021-09-24 11:47
 */
public class QuickSorter implements Sorter
{
    private int[] elements;
    private String plan;
    public String sort(int start, int end)
    {
        StringBuilder result = new StringBuilder();
        if(start < end)
        {
            int base = elements[start];
            int leftFind = start;
            int rightFind = end;
            while(leftFind < rightFind)
            {
                while(elements[rightFind] >= base && rightFind > leftFind)
                {
                    rightFind--;
                }
                if(rightFind > leftFind)
                {
                    result.append(elements[rightFind]).append("<->").append(base).append("\n");
                    elements[leftFind] = elements[rightFind];//感觉这里有改进的可能
                }
                while(elements[leftFind] <= base && leftFind < rightFind )
                {
                    leftFind++;
                }
                if(rightFind > leftFind)
                {
                    result.append(elements[leftFind]).append("<->").append(base).append("\n");
                    elements[rightFind] = elements[leftFind];
                }
            }
            elements[rightFind] = base;
            result.append(sort(start, rightFind));
            result.append(sort(rightFind + 1, end));
            return result.toString();
        }
        return "";
    }

    @Override
    public void load(int[] elements)
    {
        this.elements = elements;
    }

    @Override
    public void sort()
    {
        plan = sort(0, elements.length - 1);
    }

    @Override
    public String getPlan()
    {
        return this.plan;
    }
}
