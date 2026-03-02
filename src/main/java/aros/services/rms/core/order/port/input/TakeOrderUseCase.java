package aros.services.rms.core.order.port.input;

import aros.services.rms.core.order.application.usecases.TakeOrderCommand;
import aros.services.rms.core.order.domain.Order;

public interface TakeOrderUseCase {
    Order execute(TakeOrderCommand command);
}