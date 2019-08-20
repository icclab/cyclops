# Cyclops Estimation & Forecasting Engine Dashboard

GUI dashboard for the Cyclops Estimation and Forecasting engine.

## Prerequisites
```
pip install -r requirements.txt
```
## Start-up

- Start Cyclops as normal
- Set up `conf.ini` with the parameters necessary to interact with your
Cyclops instance
- Start the dashboard's back-end:
```
python app.py
```
- Start the dashboard itself:
```
cd dashboard
npm install
npm run dev
```
- After the first time you do not need to run `npm install`

## Usage
- Use the **Forecasting** page to generate forecasts
- Use the **Rule Management** page to add or remove rules

## Caution
It is recommended to use a separate Cyclops instance with a snapshot
of your real usage data to avoid cluttering your real database with fake records
or losing real data when performing a forecast cleanup.
