package edu.m2dl.s10.arge.openstack.calculateur;

public class Calculateur {
    public int add(int i1, int i2) {
        int res = calcul_fibonacci(i1);

        //System.out.println("\nCalcul " + i1 + " = " + res + "\n");

        return res;
    }

    private static int calcul_fibonacci(int nombre)
    {
        int term1 = 1, term2 = 1, term3 = 0;

        for (int i = 0; i < nombre; i++)
        {
            term3 = term2 + term1;
            //System.out.print(term1 + "+" +term2 + "= "+ term3 + ", ");
            term1 = term2;
            term2 = term3;
        }

        return term3;

    }

    public int subtract(int i1, int i2) {
        return i1 - i2;
    }
}