@import org.montrealjug.billetterie.entity.Booker
@import org.montrealjug.billetterie.ui.PresentationEvent
@import org.montrealjug.billetterie.ui.Utils

@param PresentationEvent event = null
@param String error = null
@param Booker booker = null
!{ var emailSignature = booker != null ? booker.getEmailSignature() : null; }
@template.layouts.guest_layout(title = "Book our next event", content = @`
<div class="flex flex-col items-center justify-center-safe h-full w-full bg-gray-100 py-2 md:p-2 lg:p-4">
    @if(error != null)
        <div id="main-error-message" class="bg-red-500 text-white p-4 mb-4 rounded-md w-full max-w-4xl">
            ${error}
        </div>
    @endif
    @if(event != null)
    <div class="w-full max-w-full h-full md:rounded md:h-auto md:max-h-full md:p-4 lg:p-8 lg:max-w-6xl flex flex-col justify-end-safe md:bg-white md:shadow-md">
        <div id="content" class="bg-gray-100 p-2 grow-1 w-full max-w-full md:rounded-md md:shadow md:p-4 overflow-y-auto flex flex-col">
            <div class="flex flex-col md:flex-row-reverse items-start justify-between w-full">
                <div class="w-full h-24 md:w-1/2">
                    @template.layouts.image_with_fallback(
                    entityType = "event",
                    cssClass = "object-contain size-full rounded-lg",
                    id = event.id()
                    )
                </div>
                <div class="md:w-1/2">
                    <h2 class="text-lg font-semibold text-gray-700">${event.title()}</h2>
                    <p class="mt-1">${Utils.DATE_FORMAT.format(event.date())}</p>
                    <p class="mt-1 text-sm md:text-base">
                        <span class="font-semibold text-gray-700">Lieu / Location:&nbsp;</span>
                        <span>${event.location()}</span>
                    </p>
                </div>
            </div>
            <details class="my-2" open>
                <summary class="hidden">Description</summary>
                $unsafe{event.description()}
            </details>
            @template.view.booker-activities(booker = booker, event = event)
        </div>
        @if (booker == null)
        <div class="flex justify-end pt-2 pr-2 md:pr-0 md:pt-4 lg:pt-8">
            <form hx-target="#modal" hx-get="/bookers/registration" hx-on::after-request="billetterie.disableRegisterButton();">
                <button id="register-button"
                        type="submit"
                        class="bg-d4k-orange text-white px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300"
                >Register</button>
            </form>
        </div>
        @else
        <div id="booker-registration-button" class="flex justify-end gap-4 pt-2 pr-2 md:pr-0 md:pt-4 lg:pt-8">
            <button form="booker-form"
                    type="submit"
                    class="bg-gray-100 text-d4k-orange opacity-75 px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-orange-300" disabled>
                Cancel</button>
            <button form="booker-form"
                    type="submit"
                    class="bg-gray-400 text-white px-3 py-2 rounded border hover:opacity-80 focus:outline-none focus:ring focus:ring-gray-300" disabled
            >Update Registration</button>
        </div>
        @endif
    </div>
    <div id="modal"></div>
    <script type="text/javascript">
        const billetterie = window.billetterie || {};
        @if (booker == null)
        if (!billetterie.disableRegisterButton) {
            billetterie.disableRegisterButton = () => {
                const button = document.getElementById("register-button");
                button.disabled = true;
                button.classList.add("hidden", "md:inline", "md:opacity-50", "md:cursor-not-allowed");
            };
        }
        @endif
        if (!billetterie.closeModal) {
            billetterie.closeModal = () => {
                document.getElementById("modal").innerHTML = "";
            };
        }
        window.billetterie = billetterie;
    </script>
    @else
    <div class="bg-white p-8 rounded shadow-md w-full max-w-4xl">
        There is no event planned currently
    </div>
    @endif
</div>
`)
