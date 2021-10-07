package example;

/**
 * 这个算法即将数据按照不同的增量分组，每组排序，然乎缩小增量，直至增量为0
 * @author ljh
 * @create 2021-09-24 14:17
 */
public class ShellSorter implements Sorter
{
    private int[] elements;
    private String plan;
    public String sort(int start, int increment)
    {
        StringBuilder result = new StringBuilder();
        int interNum;
        int preIndex;
        for (int i = start + increment; i < elements.length; i += increment)
        {
            interNum = elements[i];
            preIndex = i - increment;
            while(preIndex >= start && elements[preIndex] > interNum )
            {
                result.append(elements[preIndex] + "<->" + interNum).append("\n");
                elements[preIndex + increment] = elements[preIndex];
                preIndex -= increment;
            }
            elements[preIndex + increment] = interNum;
        }
        return result.toString();
    }

    @Override
    public void load(int[] elements)
    {
        this.elements = elements;
    }

    @Override
    public void sort()
    {
        StringBuilder sb = new StringBuilder();
        int increment = elements.length / 2;
        while(increment > 0)
        {
            for (int i = 0; i <increment; i++)
            {
                sb.append(sort(i, increment));
            }
            increment /= 2;
        }
        this.plan = sb.toString();
    }

    @Override
    public String getPlan()
    {
        return this.plan;
    }
}
