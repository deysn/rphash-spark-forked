package edu.uc.rphash.tests;

import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Leech;

public class DecoderTests {
	public static float[] cnv(double[] fff){
	float[] ret = new float[fff.length];
	for(int i = 0 ;i<fff.length;i++)ret[i] = (float)fff[i];
	return ret;
	}

	
	static void testLeechDec(){

		
		
		Decoder leech = new Leech();
		double[] f = {-0.98796955, -1.1529434 ,  1.4390883 ,  1.40046597,  0.40183179,
				-0.56885575, -0.81956525,  1.25615557, -1.29526976, -0.62859484,
				-0.7260114 ,  1.19387512,  0.74441283, -0.31003198,  1.16529063,
				0.03210929,  0.88011717,  0.98265615,  1.93322648, -0.05865583,
				-0.56355944, -0.67748379,  0.03904684, -1.0102314};


			double[] u = {0.65126908,  0.10690608,  1.16313656,  0.22987196, -1.43084181,
			0.2755519 ,  0.46149737,  1.62229512,  1.84176411,  1.14668634,
			-0.57105783, -0.25542529, -0.30482256,  0.38044721,  1.03321804,
			-0.19284389,  1.07302753, -0.50554365,  1.09262201, -0.17338258,
			-1.12363241, -0.98586661, -0.01722098,  2.4740535};

			double[] c ={-1.25952848,  0.90974636,  1.18518797,  0.71649243,  0.00428519,
					0.40136433, -0.44116449,  0.78036808,  1.22932536, -0.4739356 ,
					1.26962219,  0.73379495, -0.37507624,  0.6359808 ,  0.04275665,
					-0.06981256, -2.22652298,  2.10441221,  1.13049073, -0.1140077 ,
					-0.88809368, -1.08038432,  0.73727081,  1.02316672};
			double[] k={-0.06629867,  1.64363637, -0.27086515, -0.37690182, -0.20278382,
					0.84133612, -0.78164611,  0.5310594 ,  0.25187642,  0.56032285,
					-0.43311799,  0.34899539,  1.61118461,  0.82464746, -1.91355652,
					0.48868273, -0.69186852, -0.07240643,  0.16149872,  0.28575778,
					0.13803191, -0.18731954, -0.5343032 ,  0.67212346};
			double[] y={-2.46778603,  1.02194656, -1.21799259,  0.27824392, -0.57911542,
					0.22832422, -1.75776838, -0.09309783,  0.75097076,  0.15962876,
					0.5119343 ,  0.37938917,  0.01796803,  1.03030119, -2.64303921,
					0.32328967,  1.37198716, -0.50753097,  0.47852208,  0.10388366,
					-0.74706363, -0.66855493, -0.35686416,  0.7092663};
			double[] o = {-0.84282073,  0.59923037,  0.73899297,  1.22811334, -0.36589193,
					-0.73147463,  0.31780028,  0.99248704, -0.41232863, -0.34636915,
					0.17348888, -0.93814914, -0.05100204,  1.1133043 , -0.48937103,
					0.28450671,  0.20654879, -2.08840144, -1.72441501, -0.66277794,
					-0.72239422, -0.44093551,  1.02989744,  1.28223695};

			System.out.println(leech.decode(cnv(f)));
			System.out.println(leech.decode(cnv(u)));
			System.out.println(leech.decode(cnv(c)));
			System.out.println(leech.decode(cnv(k)));
			System.out.println(leech.decode(cnv(y)));
			System.out.println(leech.decode(cnv(o)));
		
	}
	

	public static void main(String[] args){
		testLeechDec();
	   
	}
	
}
