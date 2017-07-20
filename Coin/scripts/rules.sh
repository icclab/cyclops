## Rate Storage
curl -X "POST" "http://localhost:4570/rule" \
     -H "Content-Type: text/plain; charset=utf-8" \
     -d $'import ch.icclab.cyclops.facts.Usage;
import ch.icclab.cyclops.facts.Charge;

rule "Static rating for Storage"
salience 50
when
  $usage: Usage(metric == "Storage") 
then
  Charge charge = new Charge($usage);
  charge.setCharge(3 * $usage.getUsage());
  charge.setCurrency("CHF");

  retract($usage);
  insert(charge);
end'

echo

## Rate Memory
curl -X "POST" "http://localhost:4570/rule" \
     -H "Content-Type: text/plain; charset=utf-8" \
     -d $'import ch.icclab.cyclops.facts.Usage;
import ch.icclab.cyclops.facts.Charge;

rule "Static rating for Memory"
salience 50
when
  $usage: Usage(metric == "Memory") 
then
  Charge charge = new Charge($usage);
  charge.setCharge(2 * $usage.getUsage());
  charge.setCurrency("CHF");

  retract($usage);
  insert(charge);
end'

echo

## Rate Memory
curl -X "POST" "http://localhost:4570/rule" \
     -H "Content-Type: text/plain; charset=utf-8" \
     -d $'import ch.icclab.cyclops.facts.Usage;
import ch.icclab.cyclops.facts.Charge;

rule "Static rating for the rest"
salience 40
when
  $usage: Usage() 
then
  Charge charge = new Charge($usage);
  charge.setCharge(1 * $usage.getUsage());
  charge.setCurrency("CHF");

  retract($usage);
  insert(charge);
end'

echo

## Broadcast
curl -X "POST" "http://localhost:4570/rule" \
     -H "Content-Type: text/plain; charset=utf-8" \
     -d $'import ch.icclab.cyclops.facts.Charge;
import java.util.List;
global ch.icclab.cyclops.publish.Messenger messenger;

rule "Broadcast charge data"
salience 20
when
  $charge: List( size > 0 ) from collect ( Charge() )
then
  messenger.broadcast($charge);
  $charge.forEach(c->retract(c));
end'
