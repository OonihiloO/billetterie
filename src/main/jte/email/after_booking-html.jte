@import org.montrealjug.billetterie.email.EmailModel.AfterBookingEmail
@import org.montrealjug.billetterie.entity.ActivityParticipant

@param AfterBookingEmail context
!{var title = "Confirmation d'inscription à un événement Devoxx4Kids Québec/Booking confirmation for a Devoxx4Kids Québec event";}
!{var h1 = "Inscription confirmée!";}
@template.layouts.email_html_layout(
  title = title,
  h1 = h1,
  additionalHeadContent = @`
  <style>
    td.participant {
      padding: 0 0 0.8em 1em;
    }
    table.participant {
      border-collapse: collapse;
      border-spacing: 0;
      border: none;
      margin: 0;
      line-height: 1.6em;
    }
  </style>
  `,
  mainContent = @`
  <table class="main-table" lang="fr-CA" xml:lang="fr-CA">
    <tbody>
    <tr>
      <td>
        Bonjour ${context.booker().getFirstName()} ${context.booker().getLastName()},
      </td>
    </tr>
    <tr>
      <td>Nous vous confirmons votre inscription pour l'événement</td>
    </tr>
    <tr>
      <td class="with-top-bottom-padding">
        <strong>${context.event().getTitle()}</strong>
      </td>
    </tr>
    <tr>
      <td>
        programmé le <time datetime="${context.formatForDateTimeAttribute(context.event().getDate())}">${context.formatDateFr(context.event().getDate())}</time>.
      </td>
    </tr>
    <tr>
      <td class="with-top-bottom-padding">
        Lors de cet événement,
      </td>
    </tr>
    @for (ActivityParticipant booking: context.participants())
      <tr>
        <td class="participant">
          <table class="participant">
            <tr>
              <td>
                ${booking.getParticipant().getFirstName()} ${booking.getParticipant().getLastName()}
              </td>
            </tr>
            <tr>
              <td>participera à l'activité:</td>
            </tr>
            <tr>
              <td>
                <strong>${booking.getActivity().getTitle()}</strong>
              </td>
            </tr>
            <tr>
              <td>
                démarrant à <time datetime="${context.formatForDateTimeAttribute(booking.getActivity().getStartTime())}">${context.formatTime(booking.getActivity().getStartTime())}</time>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    @endfor
    <tr>
      <td style="padding: 0;">Vous pouvez modifier votre inscription en utilisant le lien suivant:</td>
    </tr>
    <tr>
      <td class="with-top-bottom-padding">
        <a href="${context.registrationLink()}"
           class="link-button"
           target="_blank"
        >
          @raw
          <!--[if mso]><i style="mso-font-width:80%;mso-text-raise:40%" hidden>&emsp;</i><span style="mso-text-raise:20%;"><![endif]-->
          Gérer mon inscription
          <!--[if mso]></span><i style="mso-font-width:80%;" hidden>&emsp;&#8203;</i><![endif]-->
          @endraw
        </a>
      </td>
    </tr>
    <tr>
      <td class="italic">On s'y voit bientôt!</td>
    </tr>
    <tr>
      <td class="italic">L'équipe Devoxx4Kids Québec</td>
    </tr>
    </tbody>
  </table>
  <p class="english-version">English version:</p>
  <table class="main-table" lang="en-CA" xml:lang="en-CA">
    <tbody>
    <tr>
      <td>
        Hi ${context.booker().getFirstName()} ${context.booker().getLastName()},
      </td>
    </tr>
    <tr>
      <td>We confirm your activity booking for the event</td>
    </tr>
    <tr>
      <td class="with-top-bottom-padding">
        <strong>${context.event().getTitle()}</strong>
      </td>
    </tr>
    <tr>
      <td>
        scheduled on <time datetime="${context.formatForDateTimeAttribute(context.event().getDate())}">${context.formatDateEn(context.event().getDate())}</time>.
      </td>
    </tr>
    <tr>
      <td class="with-top-bottom-padding">
        During this event,
      </td>
    </tr>
    @for (ActivityParticipant booking: context.participants())
      <tr>
        <td class="participant">
          <table class="participant">
            <tr>
              <td>
                ${booking.getParticipant().getFirstName()} ${booking.getParticipant().getLastName()}
              </td>
            </tr>
            <tr>
              <td>will attend the activity:</td>
            </tr>
            <tr>
              <td>
                <strong>${booking.getActivity().getTitle()}</strong>
              </td>
            </tr>
            <tr>
              <td>
                starting at <time datetime="${context.formatForDateTimeAttribute(booking.getActivity().getStartTime())}">${context.formatTime(booking.getActivity().getStartTime())}</time>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    @endfor
    <tr>
      <td style="padding: 0;">You can manage your booking using the following link:</td>
    </tr>
    <tr>
      <td class="with-top-bottom-padding">
        <a href="${context.registrationLink()}"
           class="link-button"
           target="_blank"
        >
          @raw
          <!--[if mso]><i style="mso-font-width:80%;mso-text-raise:40%" hidden>&emsp;</i><span style="mso-text-raise:20%;"><![endif]-->
          Manage my booking
          <!--[if mso]></span><i style="mso-font-width:80%;" hidden>&emsp;&#8203;</i><![endif]-->
          @endraw
        </a>
      </td>
    </tr>
    <tr>
      <td class="italic">See you soon there!</td>
    </tr>
    <tr>
      <td class="italic">The Devoxx4Kids Québec team</td>
    </tr>
    <!-- ghost line to avoid a strange border in Outlook classic 🤷-->
    <tr>
      <td style="padding: 0; line-height: 1em;">&nbsp;</td>
    </tr>
    </tbody>
  </table>
`)

