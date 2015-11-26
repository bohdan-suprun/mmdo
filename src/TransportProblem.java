import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by bod on 26.11.15.
 */
public class TransportProblem {
    private int currentN = -1;
    private int currentM = -1;
    private double[][] X;
    private double[][] costMatrix;
    private double bVector[];
    private double aVector[];
    private int n;
    private int m;
    private double[][] evalMatrix;
    private double bound = 99999999999l;

    public TransportProblem(InputStream is) {
        try (Scanner sc = new Scanner(is)){
            int i = 0;
            while (sc.hasNext()) {
                String line = sc.nextLine();
                if (line.equals("stop")) {
                    break;
                }
                String[] t = line.split(" ");
                double[] temp =  new double[t.length];
                for (int j = 0; j < temp.length; j++) {
                    temp[j] = Double.valueOf(t[j]);
                }
                if (i == 0) {
                    aVector = temp;
                    n = aVector.length;
                } else if (i == 1){
                    bVector = temp;
                    m = bVector.length;
                } else {
                    if (i == 2){
                        costMatrix = new double[n][];
                    }
                    costMatrix[i - 2] = temp;
                }
                i ++;
            }
            evalMatrix = new double[n][m];
            X = new double[n][m];

            for (double[] v: evalMatrix) {
                Arrays.fill(v, -1);
            }

        }
    }

    private void getBeginPlan (){
        int u = 0;
        int v = 0;
        double[] tempBVector = new double[m];
        double[] tempAVector = new double[n];
        while (u < n && v < m) {
            if( bVector[v] - tempBVector[v] < aVector[u] - tempAVector[u]) {
                double value = bVector[v] - tempBVector[v];
                X[u][v] = value;
                tempBVector[v] += value;
                tempAVector[u] += value;
                v += 1;
            }
            else {
                double z = aVector[u] - tempAVector[u];
                X[u][v] = z;
                tempBVector[v] += z;
                tempAVector[u] += z;
                u += 1;
            }
        }
    }

    private boolean isOptimal(){
        double nMax = - bound;
        buildEvalMatrix();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double x = evalMatrix[i][j];
                if ( x > nMax) {
                    nMax = x;
                    currentN = i;
                    currentM = j;
                }
            }
        }
        return nMax < 0;
    }

    private void buildEvalMatrix (){
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < m; v++) {
                evalMatrix[u][v] = -0.5;
                if (X[u][v] == 0) {
                    ArrayList<int[]> aPath = findPath(u, v);
                    int z = -1;
                    int x = 0;
                    for( int[] w:  aPath) {
                        x += z * costMatrix[w[0]][w[1]];
                        z *= -1;
                    }
                    evalMatrix[u][v] = x;
                }
            }
        }
    }

    private ArrayList<int[]> findPath(int uDest, int vDest){
        ArrayList<int[]> aPath = new ArrayList<>();
        aPath.add(new int[]{uDest, vDest});
        goHorizontal(aPath, uDest, vDest, uDest, vDest);
        return aPath;
    }

    private boolean goHorizontal(ArrayList<int[]> x, int uDest, int vDest, int u1, int v1){
        for (int i = 0; i < m; i++) {
            if (i != vDest && X[uDest][i] != 0) {
                if (i == v1) {
                    x.add(new int[]{uDest, i});
                    return true;
                }
                if (goVertical(x, uDest, i, u1, v1)) {
                    x.add(new int[]{uDest, i});
                    return true;
                }
            }
        }
        return false;
    }

    private boolean goVertical(ArrayList<int[]> x, int uDest, int vDest, int u1, int v1) {
        for (int i = 0; i < n; i++) {
            if (i != uDest && X[i][vDest] != 0) {
                if(goHorizontal(x, i, vDest, u1, v1)) {
                    x.add(new int[]{i, vDest});
                    return true;
                }
            }
        }
        return false;
    }

    private void  optimize() {
        ArrayList<int[]> aPath = findPath(currentN, currentM);
        double nMin = bound;
        for (int i = 1; i < aPath.size(); i+=2) {
            int[] w = aPath.get(i);
            double t = X[w[0]][w[1]];
            if(t<nMin) {
                nMin = t;
            }
        }

        for (int i = 1; i < aPath.size(); i+=2) {
            int[] w = aPath.get(i);
            int[] wPre = aPath.get(i - 1);
            X[w[0]][w[1]] -= nMin;
            X[wPre[0]][wPre[1]] += nMin;
        }
    }

    @Override
    public String toString() {
        buildEvalMatrix();
        double cost = 0;
        StringBuilder result = new StringBuilder();
        result.append("Опорный план:\n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                cost += costMatrix[i][j] * X[i][j];
                result.append(X[i][j]).append("     ");
            }
            result.append('\n');
        }
        result.append('\n');
        result.append("Оценочная матрица:\n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                result.append(-evalMatrix[i][j]).append("     ");
            }
            result.append('\n');
        }
        result.append("F: ").append(cost);
        return result.toString();
    }

    public void run () {
        getBeginPlan();
        System.out.println(this);
        System.out.println("<----------END ITERATION------------>");
        while(!isOptimal()) {
            System.out.println("Минимальный элемент оценочной матрицы: "+ -evalMatrix[currentN][currentM]);
            optimize();
            System.out.println(this);
            System.out.println("<----------END ITERATION------------>");
        }
    }

    public static void main(String[] args) {
        System.out.println("Введите вектор а, затем вектор b. Затем построчно введите матрицу стоимостей.\n" +
                "В конце введите слово stop");
        TransportProblem problem = new TransportProblem(System.in);
        problem.run();
    }
}
