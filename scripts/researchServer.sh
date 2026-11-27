#/bin/bash

port=9002;

echo "Veuillez patienter, ca peut prendre du temps"
echo Chargement...

for numRoom in {01..24};
do
	for numComputer in {01..30};
	do
		ping -c1 -W0.1 di-7$numRoom-$numComputer &> /dev/null;
		
		if [[ $? == 0 ]]; then
			nc -w1 di-7$numRoom-$numComputer $port 2> /dev/null;
			if [[ $? == 0 ]]; then
				exit 0;
			fi;
		fi;
	done
done

echo "Aucun serveur de lancé :[";
exit 1;
