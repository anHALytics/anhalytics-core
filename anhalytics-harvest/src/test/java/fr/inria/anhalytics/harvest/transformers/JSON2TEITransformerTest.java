package fr.inria.anhalytics.harvest.transformers;

import junit.framework.TestCase;

public class JSON2TEITransformerTest extends TestCase {
    private JSON2TEITransformer target;

    @Override
    public void setUp() throws Exception {
        target = new JSON2TEITransformer();
    }

    public void test1() throws Exception {

        String s = target.transform("{\"id\":\"0704.0006\",\"submitter\":\"Yue Hin Pong\",\"authors\":\"Y. H. Pong and C. K. Law\",\"title\":\"Bosonic characters of atomic Cooper pairs across resonance\",\"comments\":\"6 pages, 4 figures, accepted by PRA\",\"journal-ref\":null,\"doi\":\"10.1103/PhysRevA.75.043613\",\"report-no\":null,\"categories\":\"cond-mat.mes-hall\",\"license\":null,\"abstract\":\"  We study the two-particle wave function of paired atoms in a Fermi gas with\\ntunable interaction strengths controlled by Feshbach resonance. The Cooper pair\\nwave function is examined for its bosonic characters, which is quantified by\\nthe correction of Bose enhancement factor associated with the creation and\\nannihilation composite particle operators. An example is given for a\\nthree-dimensional uniform gas. Two definitions of Cooper pair wave function are\\nexamined. One of which is chosen to reflect the off-diagonal long range order\\n(ODLRO). Another one corresponds to a pair projection of a BCS state. On the\\nside with negative scattering length, we found that paired atoms described by\\nODLRO are more bosonic than the pair projected definition. It is also found\\nthat at $(k_F a)^{-1} \\\\ge 1$, both definitions give similar results, where more\\nthan 90% of the atoms occupy the corresponding molecular condensates.\\n\",\"versions\":[{\"version\":\"v1\",\"created\":\"Sat, 31 Mar 2007 04:24:59 GMT\"}],\"update_date\":\"2015-05-13\",\"authors_parsed\":[[\"Pong\",\"Y. H.\",\"\"],[\"Law\",\"C. K.\",\"\"]]}");

        System.out.println(s);
    }
}