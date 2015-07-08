#!/bin/sh

# OPTIONS:
#    -help                        print command line options
#    -harvestDaily                harvest the publications of the yesterday
#    -harvest                     harvest the publications, if no date limitation process all repository content
#    -grobid                      extract TEIs from the downladed binaries buildTei
#    -buildTei                    build the corpus tei containing the mtds and the fulltext tei
#    -annotate                    process named entities using the NERD service
#    -index {tei|annotation}      index either the full tei or annotation into ES
#    -dFromDate "yyyy-mm-dd"      set/filter records to process by starting date
#    -dUntilDate "yyyy-mm-dd"     set/filter records to process by ending date


if [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=`which java`
fi

if [ ! -x "$JAVA" ]; then
    echo "Could not find any executable java binary. Please install java in your PATH or set JAVA_HOME"
    exit 1
fi


# Print command line usage / help
usage() {
	echo "Usage: $0 [-action] [-options]"
	echo "   -help                        print command line options"
	echo "   -harvestDaily                harvest the publications of the yesterday"
	echo "   -harvest                     harvest the publications, if no date limitation process all repository content"
	echo "   -grobid                      extract TEIs from the downladed binaries buildTei"
	echo "   -buildTei                    build the corpus tei containing the mtds and the fulltext tei"
	echo "   -annotate                    process named entities using the NERD service"
	echo "   -index {tei|annotation}      index either the full tei or annotation into ES"
	echo "   -dFromDate 'yyyy-mm-dd'      set/filter records to process by starting date"
	echo "   -dUntilDate 'yyyy-mm-dd'     set/filter records to process by ending date"
}

k=0
for p in ./anhalytics-*/target
do
    directories[$k]=$p
    k=$((k + 1))
done


if [ ${#directories[@]} -lt 3 ]; then
  echo "Make sur the project is compiled."
exit 0
fi

i=0
for m in ./anhalytics-*
do
    modules[$i]=$m
    i=$((i + 1))
done


while true; do
    case $1 in
	      -help)
            usage
            exit 0
            ;;
        -harvestDaily)					
            eval cd "${modules[3]}"; j=0
						for n in ./target/*.one-jar.jar
						do
					    targets[$j]=$n
					    j=$((j + 1))
					  done;
					  "$JAVA" -Xmx2048m -jar "${targets[0]}" -exe harvestDaily
            exit 0
            ;;
        -harvest)
				    eval cd "${modules[3]}"; j=0
				    for n in ./target/*.one-jar.jar
				    do
			        targets[$j]=$n
			        j=$((j + 1))
			     done;
			     "$JAVA" -Xmx2048m -jar "${targets[0]}" -exe harvestAll $2 $3 $4 $5
           exit 0
           ;;
        -grobid)
            eval cd "${modules[3]}"; j=0
				    for n in ./target/*.one-jar.jar
				    do
			        targets[$j]=$n
			        j=$((j + 1))
			     done;
			     "$JAVA" -Xmx2048m -jar "${targets[0]}" -exe processGrobid $2 $3 $4 $5
           exit 0
           ;;
        -buildTei)
            eval cd "${modules[3]}"; j=0
				    for n in ./target/*.one-jar.jar
				    do
			        targets[$j]=$n
			        j=$((j + 1))
			     done;
			     "$JAVA" -Xmx2048m -jar "${targets[0]}" -exe buildTei $2 $3 $4 $5
           exit 0
           ;;
        -annotate)
            eval cd "${modules[0]}"; j=0
				    for n in ./target/*.one-jar.jar
				    do
			        targets[$j]=$n
			        j=$((j + 1))
			     done;
			     "$JAVA" -Xmx2048m -jar "${targets[0]}" $2 $3 $4 $5 $6
           exit 0
           ;;
        -index)
            eval cd "${modules[4]}"; j=0
				    for n in ./target/*.one-jar.jar
				    do
			        targets[$j]=$n
			        j=$((j + 1))
			     done;
			     "$JAVA" -Xmx2048m -jar "${targets[0]}" -index $2
           exit 0
           ;;
        *)
            echo "Error parsing argument $1!" >&2
            usage
            exit 1
        ;;
    esac
done

