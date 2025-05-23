@import org.montrealjug.billetterie.email.EmailModel.AfterParticipantsChangesEmail
@import org.montrealjug.billetterie.ui.PresentationParticipant

@param AfterParticipantsChangesEmail context

Participation à ${context.event().title()}

Bonjour ${context.booker().getFirstName()} ${context.booker().getLastName()},
Suite à un désistement, votre inscription au prochain événement Devoxx4Kids a été modifiée.
Voici le nouvel état de votre inscription:
@for(PresentationParticipant participant: context.participants())
  @if(participant.isWaiting())
    ${participant.firstName()} ${participant.lastName()} (${participant.age()} ans) - SUR LISTE D'ATTENTE
  @else
    ${participant.firstName()} ${participant.lastName()} (${participant.age()} ans)
  @endif
@endfor

Vérifier ou modifier la participation : ${context.registrationLink()}

L'équipe Devoxx4Kids Québec.

English version:
${context.event().title()} participation

Hi ${context.booker().getFirstName()} ${context.booker().getLastName()},
Following a participant cancellation for the next Devoxx4Kids event, your participation has been modified.
Here is the new state of your participation:
@for(PresentationParticipant participant: context.participants())
  @if(participant.isWaiting())
    ${participant.firstName()} ${participant.lastName()} (${participant.age()} year old) - ON WAITING LIST
  @else
    ${participant.firstName()} ${participant.lastName()} (${participant.age()} year old)
  @endif
@endfor


View or update the participation : ${context.registrationLink()}
The Devoxx4Kids Québec team.
