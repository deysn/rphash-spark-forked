package edu.uc.rphash;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.stream.Stream;

import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.util.VectorUtil;
import edu.uc.rphash.Readers.ReturnMaps;

//import org.apache.spark.sql.SparkSession;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class RPHash {
	
	
	/*
	 * X - set of vectors compute the medoid of a vector set
	 */
	static float[] medoid(List<float[]> X) {                                 // should this be static  ??
		float[] ret = X.get(0);
		for (int i = 1; i < X.size(); i++) {
			for (int j = 0; j < ret.length; j++) {
				ret[j] += X.get(i)[j];
			}
		}
		for (int j = 0; j < ret.length; j++) {
			ret[j] = ret[j] / ((float) X.size());
		}
		return ret;
	}


	public static void main(String[] args) throws NumberFormatException,
			IOException, InterruptedException {

		if (args.length < 3) {
			System.out
					.print("Usage: rphash InputFile k OutputFile cutoffMult selectorRphash/Twrp [CLUSTERING_METHOD ...][OPTIONAL_ARG=value ...]\n");
		
			System.exit(0);
		}
	
//		List<float[]> data = null;	
		
//		String filename = args[0];
		
		final String filename =  "/work/deysn/rphash/data/data500.mat";
		int k = Integer.parseInt(args[1]);
		String outputFile = args[2];
		
		int N = Integer.parseInt(args[3]);  						// this is the number of vectors in the data.
		int multiplier = (int) (.5 + Math.log(N) / Math.log(2));  		// log N base 2, and round to integer																																
	

		boolean raw = false;
		  
	// This is the running of the RPhash Simple Algorithm :	
				
		if (args.length == 4) {	
			SparkConf conf = new SparkConf().setAppName("RPHashSimple_Spark");
		
			JavaSparkContext sc = new JavaSparkContext(conf);
			
			
			//make a dummy list of integers for each compute node
		    int slices =  3 ;                                              //number of compute nodes or machines
		    int n = slices;
		    List<Object> l = new ArrayList<>(n);
		    for (int i = 0; i < n; i++) {
		      l.add(i);
		    }

		    JavaRDD<Object> dataSet = sc.parallelize(l);

		    List<Long>[] topids = dataSet.map(new Function<Object, List<Long>[]>()   // this pass returns the merged topids list
		    {
				private static final long serialVersionUID = -7127935862696405148L;
				
				@Override
			      public List<Long>[] call(Object integer) {
			        return RPHashSimple.mapphase1(k,filename,multiplier);
			      }
				
		    }).reduce(new Function2<List<Long>[], List<Long>[], List<Long>[]>() {
				private static final long serialVersionUID = 4294461355112957651L;
				
		
				@Override
				public List<Long>[] call(List<Long>[] topidsandcounts1, List<Long>[] topidsandcounts2) throws Exception {

					return RPHashSimple.reducephase1(topidsandcounts1,topidsandcounts2,multiplier);
				}
			    });	
				
	// now we need to propagate the list of topids to all machines . how ? should we explicitly do it through the rdd ?
		    
		    Object[] centroids = dataSet.map(new Function<Object, Object[]>()      // this returns the merged list of centroids
    	    {
				private static final long serialVersionUID = 1L;

			@Override
    	      public Object[] call(Object o) {
    	        return RPHashSimple.mapphase2(topids,filename);                   // does this propagate the topids to all machines ?
    	      }
    	    }).
    	    reduce(new Function2<Object[], Object[], Object[]>() {

			private static final long serialVersionUID = 1L;

			@Override
    		public Object[] call(Object[] cents1, Object[] cents2) throws Exception {
    			return RPHashSimple.reducephase2(cents1,cents2,multiplier);
    		}
    	    });
		    
		  
		  //offline cluster
		    VectorUtil.writeCentroidsToFile(new File(outputFile + ".mat"),new Agglomerative3((List)centroids[0], (k/6)).getCentroids(), raw);     // changed the last argument from false, to raw
		    
		    
		    sc.close();	
		
		    
		    } 
		
// End of running for the RPHash simple algorithm.		
		
		
		
// This is the Running of the TWRP algorithm
		
		if (args.length == 5) {	
			SparkConf conf = new SparkConf().setAppName("RPHashAdaptive2Pass_Spark");
		
			JavaSparkContext sc = new JavaSparkContext(conf);
			
			
			//make a dummy list of integers for each compute node
		    int slices =  3 ;                                              //number of compute nodes or machines
		    int n = slices;
		    List<Object> l = new ArrayList<>(n);
		    for (int i = 0; i < n; i++) {
		      l.add(i);
		    }

		    JavaRDD<Object> dataSet = sc.parallelize(l);                  // distributes the null RDD to the nodes/workers

		    ReturnMaps mergedmaps = dataSet.map(new Function<Object, ReturnMaps>()   // this pass returns the merged maps
		    {
				private static final long serialVersionUID = -7127935862696401234L;
				
				@Override
			      public ReturnMaps call(Object integer) {
			        return RPHashAdaptive2Pass.findDensityModesPart_1(k,filename);
			      }
				
		    }).reduce(new Function2<ReturnMaps, ReturnMaps, ReturnMaps>() {
				private static final long serialVersionUID = 4294461355112952345L;
				
		
				@Override
				public ReturnMaps call(ReturnMaps partidandcent1,
						ReturnMaps partidandcent2) throws Exception {

					return RPHashAdaptive2Pass.mergeMaps(partidandcent1,partidandcent2);
				}
			    });	
				
		    
		    
			HashMap<Long, Long> denseSetOfIDandCount = new HashMap<Long, Long>();
			for (Long cur_id : new TreeSet<Long>(mergedmaps.IDAndCount.keySet())) 
			{
				if (cur_id >k){
		            int cur_count = (int) (mergedmaps.IDAndCount.get(cur_id).longValue());
		            long parent_id = cur_id>>>1;
		            int parent_count = (int) (mergedmaps.IDAndCount.get(parent_id).longValue());
		            
		            if(cur_count!=0 && parent_count!=0)
		            {
			            if(cur_count == parent_count) {
							denseSetOfIDandCount.put(parent_id, 0L);
							mergedmaps.IDAndCent.put(parent_id, new float[]{});
														
							denseSetOfIDandCount.put(cur_id, (long) cur_count);
							
			            }
			            else
			            {
							if(2 * cur_count > parent_count) {
								denseSetOfIDandCount.remove(parent_id);
								mergedmaps.IDAndCent.put(parent_id, new float[]{});
								
								denseSetOfIDandCount.put(cur_id, (long) cur_count);

							}
			            }
		            }
				}
			}
		
			int cutoff= k*8;
			//remove keys with support less than 1
			Stream<Entry<Long, Long>> stream = denseSetOfIDandCount.entrySet().stream().filter(p -> p.getValue() > 1);
			List<Long> sortedIDList= new ArrayList<>();
			// sort and limit the list
			stream.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			
			       .forEachOrdered(x -> sortedIDList.add(x.getKey()));
			
			Multimap<Long, float[]> multimapWeightAndCent = ArrayListMultimap.create();
			
			// compute centroids

			for (Long keys: sortedIDList)
				
			{
			  
			  multimapWeightAndCent.put((Long)(mergedmaps.IDAndCount.get(keys)), (float[]) (mergedmaps.IDAndCent.get(keys)));
					
			}
		
		
			
			List<float[]>centroids = new ArrayList<>();
			List<Float> weights =new ArrayList<>();
			
			for (Long weights2 : multimapWeightAndCent.keys())	
			
			{
		
				weights.add((float)weights2);

			}

			for (Long weight2 : multimapWeightAndCent.keySet())	

			{

				centroids.addAll(multimapWeightAndCent.get(weight2));
		
			}	

	
		
		Agglomerative3 aggloOffline =  new Agglomerative3(centroids, (k/3));
		aggloOffline.setWeights(weights);
				
		List<Centroid> finalcents = aggloOffline.getCentroids();
		
		 
	    VectorUtil.writeCentroidsToFile(new File(outputFile + ".mat"),finalcents, raw);     // changed the last argument from false, to raw
		    
		    
		sc.close();	

		    }  		
		
// End of running of the TWRP algorithm		
		
		
		
		}
		
	
}
