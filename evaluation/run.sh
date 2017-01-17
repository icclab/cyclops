python -c 'from commands.ingestion import data_ingestion_evaluation; data_ingestion_evaluation(1000)'
python -c 'from commands.udr_generation import udr_generation_evaluation; udr_generation_evaluation(100, 50)'
python -c 'from commands.udr_generation import udr_generation_evaluation_uniqueness; udr_generation_evaluation_uniqueness(100, 10, 10)'
python -c 'from commands.billing import billing_evaluation; billing_evaluation(20, 5)'
python -c 'from commands.billing import billing_complexity_evaluation; billing_complexity_evaluation(20, 1000, 10)'

